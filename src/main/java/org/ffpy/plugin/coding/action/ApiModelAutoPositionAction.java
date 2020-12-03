package org.ffpy.plugin.coding.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import org.ffpy.plugin.coding.EventEnv;
import org.ffpy.plugin.coding.constant.AnnotationNames;
import org.ffpy.plugin.coding.util.ActionShowHelper;
import org.ffpy.plugin.coding.util.PsiUtils;
import org.ffpy.plugin.coding.util.SpringContextUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ApiModelAutoPositionAction extends BaseAnAction {

    private static final String POSITION_ATTR_NAME = "position";
    private static final String HIDDEN_ATTR_NAME = "hidden";

    @Autowired
    private EventEnv env;

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
                .and(this::isDumbAware)
                .classWithAnnotation(AnnotationNames.API_MODEL)
                .update();
    }
}
