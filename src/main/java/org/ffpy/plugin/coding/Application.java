package org.ffpy.plugin.coding;

import com.intellij.codeInsight.intention.IntentionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.ffpy.plugin.coding.action.env.ActionEnv;
import org.ffpy.plugin.coding.action.env.ActionEnvImpl;
import org.ffpy.plugin.coding.action.env.ActionEnvProxy;
import org.ffpy.plugin.coding.action.generate.AddMappingIgnoreAction;
import org.ffpy.plugin.coding.action.intention.BaseIntentionAction;
import org.ffpy.plugin.coding.action.intention.InsertTimestampAction;
import org.ffpy.plugin.coding.action.menu.*;
import org.ffpy.plugin.coding.config.ActionEvnConfig;
import org.ffpy.plugin.coding.util.SpringContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.List;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

    private volatile static boolean isStarted = false;

    @Autowired
    private List<BaseIntentionAction> intentionActionList;

    public static void start() {
        if (isStarted) return;
        isStarted = true;

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

    @Bean
    public SpringContextUtils springContextUtils() {
        return new SpringContextUtils();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public AddMappingIgnoreAction addMappingIgnoreAction() {
        return new AddMappingIgnoreAction();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public InsertTimestampAction insertTimestampAction() {
        return new InsertTimestampAction();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ApiModelAutoPositionAction apiModelAutoPositionAction() {
        return new ApiModelAutoPositionAction();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CaseFormatAction caseFormatAction() {
        return new CaseFormatAction();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FormatSqlAction formatSqlAction() {
        return new FormatSqlAction();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public GenerateGetterCallerAction generateGetterCallerAction() {
        return new GenerateGetterCallerAction();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public GenerateSetterCallerAction generateSetterCallerAction() {
        return new GenerateSetterCallerAction();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public NewEnumCodeAction newEnumCodeAction() {
        return new NewEnumCodeAction();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SortFieldAction sortFieldAction() {
        return new SortFieldAction();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SortMethodAction sortMethodAction() {
        return new SortMethodAction();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public WrapBackquoteAction wrapBackquoteAction() {
        return new WrapBackquoteAction();
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
