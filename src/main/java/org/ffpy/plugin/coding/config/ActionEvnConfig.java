package org.ffpy.plugin.coding.config;

import com.intellij.openapi.actionSystem.AnActionEvent;

import java.util.Objects;

public class ActionEvnConfig {

    public static final ThreadLocal<AnActionEvent> event = new ThreadLocal<>();

    public static void setEvent(AnActionEvent event) {
        ActionEvnConfig.event.set(Objects.requireNonNull(event));
    }

    public static void clearEvent() {
        ActionEvnConfig.event.set(null);
    }
}
