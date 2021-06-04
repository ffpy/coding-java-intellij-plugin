package org.ffpy.plugin.coding.action.menu

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiClass
import org.ffpy.plugin.coding.action.BaseAction
import org.ffpy.plugin.coding.util.ActionShowHelper
import org.ffpy.plugin.coding.util.MyStringUtils

/**
 * 格式化字段名，把字段名转换为驼峰式
 */
class NormalFieldNameAction : BaseAction() {

    override fun action() {
        val curClass: PsiClass = env.selectedClass ?: env.curClass ?: return
        curClass.fields.forEach {
            env.writeActions.add { it.name = normalName(it.name) }
        }
        env.writeActions.run()
    }

    override fun update(e: AnActionEvent) {
        ActionShowHelper.of(e)
            .isJavaFile()
            .update()
    }

    private fun normalName(name: String): String {
        val s = if (isAllUpperCase(name)) name.toLowerCase() else name
        return if (name.contains("_")) MyStringUtils.underScoreCase2CamelCase(s) else s
    }

    private fun isAllUpperCase(s: String): Boolean {
        return s.toCharArray().filter { it.isLetter() }.all { it.isUpperCase() }
    }
}