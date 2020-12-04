package org.ffpy.plugin.coding.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class WriteActions {
    private final Project project;
    private final List<Runnable> tasks = new LinkedList<>();

    public WriteActions(Project project) {
        this.project = project;
    }

    public WriteActions add(Runnable task) {
        tasks.add(task);
        return this;
    }

    public WriteActions clear() {
        tasks.clear();
        return this;
    }

    public void run() {
        if (!tasks.isEmpty()) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                for (Runnable task : tasks) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
                tasks.clear();
            });
        }
    }
}
