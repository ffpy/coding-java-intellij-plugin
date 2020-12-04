package org.ffpy.plugin.coding.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import lombok.extern.slf4j.Slf4j;
import org.ffpy.plugin.coding.action.env.ActionEnv;
import org.ffpy.plugin.coding.config.ActionEvnConfig;
import org.ffpy.plugin.coding.util.NotificationHelper;
import org.ffpy.plugin.coding.util.SpringContextUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class BaseAction extends AnAction implements DumbAware {

    public abstract void action() throws Exception;

    @Autowired
    protected ActionEnv env;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            ActionEvnConfig.setEvent(event);
            SpringContextUtils.getBean(getClass()).action();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            NotificationHelper.error(e.getMessage()).show();
        } finally {
            ActionEvnConfig.clearEvent();
        }
    }
}
