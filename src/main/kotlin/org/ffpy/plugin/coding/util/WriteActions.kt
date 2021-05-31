package org.ffpy.plugin.coding.util

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import java.util.*

class WriteActions(private val project: Project) {
    private val tasks: MutableList<Runnable> = LinkedList()
    fun add(task: Runnable): WriteActions {
        tasks.add(task)
        return this
    }

    fun clear(): WriteActions {
        tasks.clear()
        return this
    }

    fun run() {
        if (tasks.isNotEmpty()) {
            WriteCommandAction.runWriteCommandAction(project) {
                for (task in tasks) {
                    try {
                        task.run()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                tasks.clear()
            }
        }
    }
}