package org.ffpy.plugin.coding.component;

import com.intellij.codeInsight.intention.IntentionManager;
import lombok.extern.slf4j.Slf4j;
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
        log.info("错误日志位置：{}", new File("log/CodingPlugin.log").getAbsolutePath());

        IntentionManager manager = IntentionManager.getInstance();
    }
}
