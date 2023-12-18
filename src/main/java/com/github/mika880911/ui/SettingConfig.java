package com.github.mika880911.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import javax.swing.*;
import java.util.ArrayList;

public class SettingConfig implements Configurable
{
    private SettingForm form = new SettingForm();

    private SettingState state = ApplicationManager.getApplication().getService(SettingState.class);

    public String getDisplayName()
    {
        return "AI Tool Setting";
    }

    public JComponent createComponent()
    {
        this.setupComboBoxOptions(this.form.baseURLComboBox, this.state.getBaseURLs());
        this.setupComboBoxOptions(this.form.apiKeyComboBox, this.state.getApiKeys());
        this.setupComboBoxOptions(this.form.modelComboBox, this.state.getModels());

        return this.form.mainPanel;
    }

    public boolean isModified()
    {
        return ! this.state.baseURL.equals(this.form.baseURLComboBox.getEditor().getItem().toString())
                || ! this.state.apiKey.equals(this.form.apiKeyComboBox.getEditor().getItem().toString())
                || ! this.state.model.equals(this.form.modelComboBox.getEditor().getItem().toString());
    }

    public void apply() throws ConfigurationException
    {
        this.state.baseURL = this.form.baseURLComboBox.getEditor().getItem().toString();
        this.state.apiKey = this.form.apiKeyComboBox.getEditor().getItem().toString();
        this.state.model = this.form.modelComboBox.getEditor().getItem().toString();
    }

    public void reset()
    {
        this.form.baseURLComboBox.getEditor().setItem(this.state.baseURL);
        this.form.apiKeyComboBox.getEditor().setItem(this.state.apiKey);
        this.form.modelComboBox.getEditor().setItem(this.state.model);
    }

    public void setupComboBoxOptions(JComboBox comboBox, ArrayList<String> options)
    {
        comboBox.removeAllItems();

        options.forEach((option) -> {
            comboBox.addItem(option);
        });
    }
}
