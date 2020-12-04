package org.ffpy.plugin.coding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

    public static void start() {
        Thread thread = new Thread(() -> SpringApplication.run(Application.class));
        thread.setContextClassLoader(null);
        thread.start();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Application run success.");
    }
}