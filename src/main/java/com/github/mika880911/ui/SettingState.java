package com.github.mika880911.ui;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import java.util.ArrayList;

@Service
@State(name = "SettingState", storages = {@Storage("ai-tool-setting-state.xml")})
public final class SettingState implements PersistentStateComponent<SettingState>
{
    public String baseURL = this.getBaseURLs().get(0);

    public String apiKey = this.getApiKeys().get(0);

    public String model = this.getModels().get(0);

    public SettingState getState()
    {
        return this;
    }

    public void loadState(SettingState state)
    {
        XmlSerializerUtil.copyBean(state, this);
    }

    public ArrayList<String> getBaseURLs()
    {
        return new ArrayList<>() {
            {
                add("https://api.openai.com/v1");
                add("http://localhost:1234/v1");
            }
        };
    }

    public ArrayList<String> getApiKeys()
    {
        return new ArrayList<>() {
            {
                add("sk-xxx");
            }
        };
    }

    public ArrayList<String> getModels()
    {
        return new ArrayList<>() {
            {
                add("gpt-4-32k");
                add("gpt-3.5-turbo-16k");
                add("vicuna-13B-v1.5-16K");
            }
        };
    }
}
