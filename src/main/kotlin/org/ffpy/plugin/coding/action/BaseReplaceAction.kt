package org.ffpy.plugin.coding.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor

abstract class BaseReplaceAction : BaseAction() {

    /**
     * @param e    AnActionEvent
     * @param text 选择的文本
     * @return 改变后的文本，可以为null表示不改变
     * @throws Exception 异常
     */
    protected abstract fun replace(e: AnActionEvent, text: String): String?

    override fun action() {
        val editor: Editor = env.editor ?: return
        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) return
        val selectedText = selectionModel.selectedText ?: return
        val start = selectionModel.selectionStart
        val end = selectionModel.selectionEnd
        val newText = replace(env.event, selectedText) ?: return
        WriteCommandAction.runWriteCommandAction(
            env.project
        ) { editor.document.replaceString(start, end, newText) }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null && hasSelectedText(e)
    }

    private fun hasSelectedText(e: AnActionEvent): Boolean {
        return e.getData(LangDataKeys.EDITOR)?.selectionModel?.selectedText?.isNotEmpty() ?: false
    }
}