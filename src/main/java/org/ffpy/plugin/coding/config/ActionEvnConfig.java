package org.ffpy.plugin.coding.config;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.ffpy.plugin.coding.action.env.ActionEnv;
import org.ffpy.plugin.coding.action.env.ActionEnvImpl;
import org.ffpy.plugin.coding.action.env.ActionEnvProxy;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Objects;

@Configuration
public class ActionEvnConfig {

    private static final ThreadLocal<AnActionEvent> event = new ThreadLocal<>();

    public static void setEvent(AnActionEvent event) {
        ActionEvnConfig.event.set(Objects.requireNonNull(event));
    }

    public static void clearEvent() {
        ActionEvnConfig.event.set(null);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ActionEnv eventEnv() {
        AnActionEvent event = ActionEvnConfig.event.get();
        if (event == null) {
            throw new RuntimeException("event not found.");
        }
        ActionEnvImpl target = new ActionEnvImpl(event);
        ActionEnv proxy = ActionEnvProxy.getInstance(target);
        target.setSelf(proxy);
        return proxy;
    }
}
