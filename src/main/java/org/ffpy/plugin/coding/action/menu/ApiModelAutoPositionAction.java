package org.ffpy.plugin.coding.action.menu;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import org.ffpy.plugin.coding.action.ActionService;
import org.ffpy.plugin.coding.action.BaseAction;
import org.ffpy.plugin.coding.constant.AnnotationNames;
import org.ffpy.plugin.coding.util.ActionShowHelper;
import org.ffpy.plugin.coding.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * ApiModel自动编号
 */
@ActionService
public class ApiModelAutoPositionAction extends BaseAction {

    private static final String POSITION_ATTR_NAME = "position";
    private static final String HIDDEN_ATTR_NAME = "hidden";

    @Override
    public void action() throws Exception {
        int pos = 1;
        for (PsiField field : env.getCurClassOrThrow().getFields()) {
            PsiAnnotation anno = field.getAnnotation(AnnotationNames.API_MODEL_PROPERTY);
            if (anno != null) {
                Boolean hidden = PsiUtils.getAnnotationValue(anno, HIDDEN_ATTR_NAME, Boolean.class);
                PsiExpression value;
                if (Objects.equals(hidden, true)) {
                    value = null;
                } else {
                    value = env.getElementFactory()
                            .createExpressionFromText(String.valueOf(pos++), null);
                }
                env.getWriteActions().add(() ->
                        anno.setDeclaredAttributeValue(POSITION_ATTR_NAME, value));
            }
        }
        env.getWriteActions().run();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ActionShowHelper.of(e)
                .classWithAnnotation(AnnotationNames.API_MODEL)
                .update();
    }
}
