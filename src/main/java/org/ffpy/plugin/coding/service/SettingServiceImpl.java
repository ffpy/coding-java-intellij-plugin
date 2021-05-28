package org.ffpy.plugin.coding.service;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.sun.istack.Nullable;
import org.ffpy.plugin.coding.constant.TemplateName;

import java.io.IOException;
import java.util.Arrays;

public class SettingServiceImpl implements SettingService {

    private static final String KEY_TEMPLATE = "template_";
    private static final String KEY_TRANSLATE_APP_ID = "translate_app_id";
    private static final String KEY_TRANSLATE_SECRET = "translate_secret";

    private final Project mProject;
    private final PropertiesComponent projectProperties;
    private final PropertiesComponent applicationProperties;

    public SettingServiceImpl(Project project) {
        mProject = project;
        projectProperties = PropertiesComponent.getInstance(mProject);
        applicationProperties = PropertiesComponent.getInstance();
    }

    @Override
    public String getTranslateAppId() {
        return applicationProperties.getValue(KEY_TRANSLATE_APP_ID);
    }

    @Override
    public void setTranslateAppId(String appId) {
        applicationProperties.setValue(KEY_TRANSLATE_APP_ID, appId);
    }

    @Override
    public String getTranslateSecret() {
        return applicationProperties.getValue(KEY_TRANSLATE_SECRET);
    }

    @Override
    public void setTranslateSecret(String secret) {
        applicationProperties.setValue(KEY_TRANSLATE_SECRET, secret);
    }

    @Override
    public String getTemplate(TemplateName name) {
        String template = projectProperties.getValue(getTemplateKey(name));
        if (template == null) {
            try {
                template = FileUtil.loadTextAndClose(getClass().getResourceAsStream(name.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return template;
    }

    @Override
    public void setTemplate(TemplateName name, @Nullable String template) {
        if (template == null) {
            projectProperties.unsetValue(getTemplateKey(name));
        } else {
            projectProperties.setValue(getTemplateKey(name), template);
        }
    }

    @Override
    public void reset() {
        Arrays.stream(TemplateName.values())
                .forEach(name -> setTemplate(name, null));
    }

    private String getTemplateKey(TemplateName name) {
        return KEY_TEMPLATE + name.getName();
    }
}
