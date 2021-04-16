package org.ffpy.plugin.coding.component;

import lombok.extern.slf4j.Slf4j;
import org.ffpy.plugin.coding.Application;
import org.ffpy.plugin.coding.util.NotificationHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Slf4j
public class AppComponentImpl implements AppComponent {

    @NotNull
    @Override
    public String getComponentName() {
        return "Coding.AppComponent";
    }

    @Override
    public void initComponent() {
//        NotificationHelper.info("initComponent").show();
        Application.start();
    }
}
