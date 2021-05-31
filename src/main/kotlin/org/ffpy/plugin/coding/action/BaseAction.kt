package org.ffpy.plugin.coding.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import org.ffpy.plugin.coding.util.NotificationHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseAction : AnAction(), DumbAware {

    companion object {
        val log: Logger = LoggerFactory.getLogger(BaseAction::class.java)
    }

    private val envThreadLocal = ThreadLocal<ActionEnv>()

    protected val env: ActionEnv
        get() = envThreadLocal.get()!!

    abstract fun action()

    override fun actionPerformed(e: AnActionEvent) {
        try {
            envThreadLocal.set(ActionEnv(e))
            action()
        } catch (e: Exception) {
            NotificationHelper.error(e.message).show()
            log.error(e.message, e)
        } finally {
            envThreadLocal.remove()
        }
    }
}