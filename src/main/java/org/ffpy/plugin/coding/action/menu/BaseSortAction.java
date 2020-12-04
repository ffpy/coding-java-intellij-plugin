package org.ffpy.plugin.coding.action.menu;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import org.ffpy.plugin.coding.action.BaseAction;
import org.ffpy.plugin.coding.util.ActionShowHelper;
import org.jetbrains.annotations.NotNull;

public abstract class BaseSortAction extends BaseAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        ActionShowHelper.of(e)
                .isJavaFile()
                .update();
    }

    protected int getOrder(PsiModifierList modifierList) {
        if (modifierList == null) {
            return 0;
        }
        int order = 0;

        if (modifierList.hasModifierProperty(PsiModifier.ABSTRACT)) {
            order += 10000;
        }
        if (modifierList.hasModifierProperty(PsiModifier.STATIC)) {
            order += 1000;
        }
        if (modifierList.hasModifierProperty(PsiModifier.FINAL)) {
            order += 100;
        }

        // public protected default private
        if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
            order += 10;
        } else if (modifierList.hasModifierProperty(PsiModifier.PROTECTED)) {
            order += 9;
        } else if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
            order += 7;
        } else {
            order += 8;
        }

        return order;
    }
}
