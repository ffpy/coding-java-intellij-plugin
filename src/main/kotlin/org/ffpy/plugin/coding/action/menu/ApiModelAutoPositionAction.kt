package org.ffpy.plugin.coding.action.menu

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiExpression
import org.ffpy.plugin.coding.action.BaseAction
import org.ffpy.plugin.coding.constant.AnnotationNames
import org.ffpy.plugin.coding.util.ActionShowHelper
import org.ffpy.plugin.coding.util.PsiUtils.getAnnotationValue

/**
 * ApiModel自动编号
 */
class ApiModelAutoPositionAction : BaseAction() {

    companion object {
        private const val POSITION_ATTR_NAME = "position"
        private const val HIDDEN_ATTR_NAME = "hidden"
    }

    override fun action() {
        val curClass = env.curClass ?: return
        var pos = 1
        for (field in curClass.fields) {
            val annotation = field.getAnnotation(AnnotationNames.API_MODEL_PROPERTY)
            if (annotation != null) {
                val hidden = getAnnotationValue(
                    annotation, HIDDEN_ATTR_NAME,
                    Boolean::class.java
                )
                var value: PsiExpression?
                value = if (hidden == true) {
                    null
                } else {
                    env.elementFactory.createExpressionFromText(pos++.toString(), null)
                }
                env.writeActions.add {
                    annotation.setDeclaredAttributeValue(POSITION_ATTR_NAME, value)
                }
            }
        }
        env.writeActions.run()
    }

    override fun update(e: AnActionEvent) {
        ActionShowHelper.of(e)
            .classWithAnnotation(AnnotationNames.API_MODEL)
            .update()
    }
}