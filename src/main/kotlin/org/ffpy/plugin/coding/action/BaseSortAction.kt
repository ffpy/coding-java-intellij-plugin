package org.ffpy.plugin.coding.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifierList
import org.ffpy.plugin.coding.util.ActionShowHelper

abstract class BaseSortAction : BaseAction() {

    override fun update(e: AnActionEvent) {
        ActionShowHelper.of(e)
            .isJavaFile()
            .update()
    }

    protected open fun getOrder(modifierList: PsiModifierList?): Int {
        if (modifierList == null) {
            return 0
        }
        var order = 0
        if (modifierList.hasModifierProperty(PsiModifier.ABSTRACT)) {
            order += 10000
        }
        if (modifierList.hasModifierProperty(PsiModifier.STATIC)) {
            order += 1000
        }
        if (modifierList.hasModifierProperty(PsiModifier.FINAL)) {
            order += 100
        }

        // public protected default private
        order += when {
            modifierList.hasModifierProperty(PsiModifier.PUBLIC) -> 10
            modifierList.hasModifierProperty(PsiModifier.PROTECTED) -> 9
            modifierList.hasModifierProperty(PsiModifier.PRIVATE) -> 7
            else -> 8
        }
        return order
    }
}