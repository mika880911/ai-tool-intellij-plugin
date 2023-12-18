package com.github.mika880911.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import javax.swing.*;

public class SettingConfig implements Configurable
{
    private SettingForm form = new SettingForm();

    public String getDisplayName()
    {
        return "AI Tool Setting";
    }

    public JComponent createComponent()
    {
        return this.form.mainPanel;
    }

    public boolean isModified()
    {
        return true;
    }

    public void apply() throws ConfigurationException
    {
    }

    public void reset()
    {
    }
}
