package org.ffpy.plugin.coding;

import com.intellij.codeInsight.intention.IntentionManager;
import lombok.extern.slf4j.Slf4j;
import org.ffpy.plugin.coding.action.intention.BaseIntentionAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

    @Autowired
    private List<BaseIntentionAction> intentionActionList;

    public static void start() {
        Thread thread = new Thread(() -> SpringApplication.run(Application.class));
        thread.setContextClassLoader(null);
        thread.start();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Application run success.");

        IntentionManager manager = IntentionManager.getInstance();
        intentionActionList.forEach(manager::addAction);
    }
}
