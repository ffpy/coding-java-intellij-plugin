package org.ffpy.plugin.coding.config;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.ffpy.plugin.coding.EventEnv;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Objects;

@Configuration
public class EventEvnConfig {

    private static final ThreadLocal<AnActionEvent> event = new ThreadLocal<>();

    public static void setEvent(AnActionEvent event) {
        EventEvnConfig.event.set(Objects.requireNonNull(event));
    }

    public static void clearEvent() {
        EventEvnConfig.event.set(null);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public EventEnv eventEnv() {
        AnActionEvent event = EventEvnConfig.event.get();
        if (event == null) {
            throw new RuntimeException("event not found.");
        }
        return new EventEnv(event);
    }
}
