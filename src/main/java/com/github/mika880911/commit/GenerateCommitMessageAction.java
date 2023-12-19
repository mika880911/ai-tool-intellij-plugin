package com.github.mika880911.commit;

import com.github.mika880911.ui.SettingState;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressWindow;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import git4idea.GitUtil;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitImpl;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GenerateCommitMessageAction extends AnAction
{
    private final String pluginName = PluginManagerCore.getPlugin(PluginId.getId("com.github.mika880911.ai-tool-intellij-plugin")).getName();

    public void actionPerformed(AnActionEvent e)
    {
        ProgressManager.getInstance().run(this.makeTask(e));
    }

    public Task.Backgroundable makeTask(AnActionEvent e)
    {
        return new Task.Backgroundable(e.getProject(), this.pluginName, false) {
            public void run(ProgressIndicator indicator) {
                ProgressWindow progressWindow = new ProgressWindow(false, e.getProject());
                progressWindow.setTitle(GenerateCommitMessageAction.this.pluginName);
                progressWindow.setIndeterminate(true);
                progressWindow.start();

                try {
                    String gitDiffString = GenerateCommitMessageAction.this.getGitDiff(e);
                    String message = GenerateCommitMessageAction.this.generateCommitMessage(gitDiffString);
                    GenerateCommitMessageAction.this.setCommitMessage(message, e);
                } catch (Exception ex) {
                    String errorMessage = ex.getMessage() == null ? "Unknown error" : ex.getMessage();

                    ApplicationManager.getApplication().invokeLater(() ->
                            Messages.showMessageDialog(e.getProject(), errorMessage, GenerateCommitMessageAction.this.pluginName, Messages.getInformationIcon())
                    );
                } finally {
                    progressWindow.stop();
                }
            }
        };
    }

    private String getGitDiff(AnActionEvent e) throws Exception {
        Git git = new GitImpl();

        GitRepository gitrepository = GitUtil.getRepositoryManager(e.getProject()).getRepositories().get(0);
        GitLineHandler gitLineHandler = new GitLineHandler(e.getProject(), gitrepository.getRoot(), GitCommand.DIFF);
        gitLineHandler.addParameters("--staged");

        String result = git.runCommand(gitLineHandler).getOutputAsJoinedString();

        if (result.isEmpty()) {
            throw new Exception("Staging is empty");
        }

        return result;
    }

    private void setCommitMessage(String message, AnActionEvent e) {
        ApplicationManager.getApplication().invokeLater(() -> {
            CommitMessageI commitMessage = VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.getDataContext());
            commitMessage.setCommitMessage(message);
        });
    }

    private String generateCommitMessage(String gitDiffString) throws IOException, InterruptedException {
        String prompt = new String(this.getClass().getClassLoader().getResourceAsStream("prompts/commit.template").readAllBytes()) + gitDiffString;
        SettingState state = ApplicationManager.getApplication().getService(SettingState.class);

        JSONObject jsonObject = new JSONObject() {{
            put("messages", new JSONArray() {{
                add(new JSONObject() {{
                    put("role", "user");
                    put("content", prompt);
                }});
            }});

            put("model", state.model);
        }};

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(state.baseURL + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + state.apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toJSONString()))
                .build();

        String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        JSONObject jsonResponse = (JSONObject) JSONValue.parse(response);
        JSONArray choices = (JSONArray) jsonResponse.get("choices");
        JSONObject choice = (JSONObject) choices.get(0);
        JSONObject message = (JSONObject) choice.get("message");
        JSONObject content = (JSONObject) JSONValue.parse(message.getAsString("content"));

        String body = "";

        try {
            JSONArray points = (JSONArray) JSONValue.parse(content.getAsString("body"));
            for (Object point : points) {
                body += point.toString().trim().replace('+', '-').replace('*', '-') + "\n";
            }
        } catch (Exception ex) {
            String[] points = content.getAsString("body").split("\n");
            for (Object point : points) {
                body += point.toString().trim().replace('+', '-').replace('*', '-') + "\n";
            }
        }

        return content.getAsString("type").trim() + ": " + content.getAsString("subject").trim() + "\n\n" + body;
    }
}
