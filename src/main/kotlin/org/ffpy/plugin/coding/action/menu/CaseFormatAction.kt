package org.ffpy.plugin.coding.action.menu

import com.intellij.openapi.actionSystem.AnActionEvent
import org.ffpy.plugin.coding.action.BaseReplaceAction
import org.ffpy.plugin.coding.util.MyStringUtils.camelCase2UnderScoreCase
import org.ffpy.plugin.coding.util.MyStringUtils.underScoreCase2CamelCase

/**
 * 驼峰-下划线互转
 */
class CaseFormatAction: BaseReplaceAction() {

    override fun replace(e: AnActionEvent, text: String): String? {
        return if (text.contains("_")) underScoreCase2CamelCase(text) else camelCase2UnderScoreCase(text)
    }
}