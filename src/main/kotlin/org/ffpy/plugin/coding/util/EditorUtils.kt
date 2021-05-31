package org.ffpy.plugin.coding.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import java.util.*
import java.util.regex.Pattern

object EditorUtils {

    /**
     * 获取光标所在行的文本
     *
     * @param editor Editor
     * @return 文本
     */
    fun getCurLineText(editor: Editor): String {
        val caretModel = editor.caretModel
        val line = caretModel.logicalPosition.line
        val start = if (line == 0) 0 else editor.document.getLineEndOffset(line - 1) + 1
        val end = editor.document.getLineEndOffset(line)
        return editor.document.getText(TextRange(start, end))
    }

    /**
     * 获取指定行的文本
     *
     * @param doc  文档
     * @param line 行号(0到doc.getLineCount()-1)
     * @return 文本
     */
    fun getLineText(doc: Document, line: Int): String? {
        if (line < 0 || line >= doc.lineCount) {
            return null
        }
        val start = doc.getLineStartOffset(line)
        val end = doc.getLineEndOffset(line)
        return doc.getText(TextRange(start, end))
    }

    /**
     * 插入内容在光标所在行的下一行
     *
     * @param editor  Editor
     * @param content 要插入的文本
     */
    fun insertToNextLine(editor: Editor, content: String?) {
        val position = editor.caretModel.logicalPosition
        val lineEndOffset = editor.document.getLineEndOffset(position.line)
        WriteActions(editor.project!!)
            .add { editor.document.insertString(lineEndOffset, content!!) }
            .run()
    }

    /**
     * 在当前打开的编辑框中查找指定顶部类的Editor
     *
     * @param className 顶部类类名
     * @return Editor，找不到则为null
     */
    fun getEditorByClassName(className: String): Optional<Editor> {
        val pattern = Pattern.compile("^public\\s+class\\s+$className", Pattern.MULTILINE)
        val editors = EditorFactory.getInstance().allEditors
        for (i in editors.indices.reversed()) {
            val editor = editors[i]
            if (pattern.matcher(editor.document.text).find()) {
                return Optional.of(editor)
            }
        }
        return Optional.empty()
    }

    /**
     * 光标移动到指定位置
     *
     * @param editor 编辑器
     * @param offset 位置
     */
    fun moveToOffset(editor: Editor, offset: Int) {
        editor.caretModel.moveToOffset(offset)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
    }

    /**
     * 光标移动到类的指定位置
     *
     * @param psiClass 类
     * @param offset   位置
     */
    fun moveToClassOffset(psiClass: PsiClass, offset: Int, writeActions: WriteActions) {
        writeActions.add {
            FileUtils.navigateFileInEditor(
                psiClass.project, psiClass.containingFile.virtualFile
            )
        }.add {
            getEditorByClassName(psiClass.name!!).ifPresent { editor: Editor -> moveToOffset(editor, offset) }
        }.run()
    }
}