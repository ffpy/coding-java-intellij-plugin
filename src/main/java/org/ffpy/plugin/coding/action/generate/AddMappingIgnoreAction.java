package org.ffpy.plugin.coding.action.generate;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.ffpy.plugin.coding.action.ActionService;
import org.ffpy.plugin.coding.action.BaseAction;
import org.ffpy.plugin.coding.constant.AnnotationNames;
import org.ffpy.plugin.coding.util.ActionShowHelper;
import org.ffpy.plugin.coding.util.IndexUtils;
import org.ffpy.plugin.coding.util.PsiUtils;
import org.ffpy.plugin.coding.util.WriteActions;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapping自动添加ignore
 */
@ActionService
public class AddMappingIgnoreAction extends BaseAction {

    @Override
    public void action() throws Exception {
        PsiModifierList curMethodModifierList = getCurMethodModifierList().orElse(null);
        if (curMethodModifierList == null) return;

        getIgnoreFields().forEach(field -> {
            PsiAnnotation anno = env.getElementFactory().createAnnotationFromText(
                    "@Mapping(target = \"" + field + "\", ignore = true)", null);
            env.getWriteActions().add(() -> curMethodModifierList.add(anno));
        });
        env.getWriteActions().run();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ActionShowHelper.of(e)
                .classWithAnnotation(AnnotationNames.MAPPER)
                .elementType(PsiMethod.class)
                .update();
    }

    private Optional<PsiModifierList> getCurMethodModifierList() {
        return env.getSelectedMethod()
                .map(it -> PsiTreeUtil.getChildOfType(it, PsiModifierList.class));
    }

    /**
     * 获取返回类的字段列表
     */
    private Set<String> getReturnFields() {
        PsiMethod selectedMethod = env.getSelectedMethod().orElse(null);
        if (selectedMethod == null) return Collections.emptySet();
        Project project = env.getProject();

        return Optional.ofNullable(selectedMethod.getReturnType())
                .flatMap(type -> Optional.ofNullable(
                        IndexUtils.getClassByQualifiedName(project, type.getCanonicalText())))
                .map(psiClass -> {
                    List<String> list = PsiUtils.getAllSetterName(psiClass)
                            .collect(Collectors.toList());
                    // 处理XXXRecord的valueXX方法
                    list.addAll(Arrays.stream(psiClass.getAllMethods())
                            .filter(method -> {
                                PsiModifierList modifierList = method.getModifierList();
                                return modifierList.hasModifierProperty(PsiModifier.PUBLIC) &&
                                        !modifierList.hasModifierProperty(PsiModifier.STATIC);
                            })
                            .filter(method -> method.getName().matches("value\\d+"))
                            .map(PsiMethod::getName)
                            .collect(Collectors.toList()));
                    return list;
                })
                // 保持顺序
                .map(list -> (Set<String>) new LinkedHashSet<>(list))
                .orElse(Collections.emptySet());
    }

    /**
     * 获取参数字段列表
     */
    private Set<String> getParameterArgs() {
        PsiMethod selectedMethod = env.getSelectedMethod().orElse(null);
        if (selectedMethod == null) return Collections.emptySet();

        Set<String> args = new LinkedHashSet<>();
        for (PsiParameter parameter : selectedMethod.getParameterList().getParameters()) {
            PsiClass parameterClass = IndexUtils.getClassByQualifiedName(env.getProject(),
                    parameter.getType().getCanonicalText());

            boolean isCustomClass = Optional.ofNullable(parameterClass)
                    .map(PsiClass::getQualifiedName)
                    .map(name -> !name.startsWith("java.lang"))
                    .orElse(false);

            if (isCustomClass) {
                args.addAll(PsiUtils.getAllGetterName(parameterClass)
                        .collect(Collectors.toSet()));
            } else {
                args.add(parameter.getName());
            }
        }
        return args;
    }

    /**
     * 获取要忽略的字段列表
     */
    private Set<String> getIgnoreFields() {
        PsiMethod selectedMethod = env.getSelectedMethod().orElse(null);
        if (selectedMethod == null) return Collections.emptySet();

        Set<String> set = new LinkedHashSet<>(getReturnFields());
        set.removeAll(getParameterArgs());

        Arrays.stream(selectedMethod.getAnnotations())
                .filter(anno -> AnnotationNames.MAPPING.equals(anno.getQualifiedName()))
                .map(anno -> PsiUtils.getAnnotationValue(anno, "target", String.class))
                .filter(Objects::nonNull)
                .forEach(set::remove);

        return set;
    }
}
