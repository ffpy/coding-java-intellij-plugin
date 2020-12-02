package org.ffpy.plugin.coding.service;

import com.sun.istack.Nullable;
import org.ffpy.plugin.coding.constant.TemplateName;

public interface SettingService {

    String getPackageName();

    void setPackageName(@Nullable String packageName);

    String getTranslateAppId();

    void setTranslateAppId(String appId);

    String getTranslateSecret();

    void setTranslateSecret(String secret);

    String getTemplate(TemplateName name);

    void setTemplate(TemplateName name, @Nullable String content);

    void reset();
}
