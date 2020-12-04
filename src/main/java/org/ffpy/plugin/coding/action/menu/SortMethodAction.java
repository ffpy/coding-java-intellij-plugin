package org.ffpy.plugin.coding.action.menu;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.ffpy.plugin.coding.action.ActionService;
import org.ffpy.plugin.coding.util.PsiUtils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 方法排序
 */
@ActionService
public class SortMethodAction extends BaseSortAction {

    @Override
    public void action() throws Exception {
        PsiClass curClass = env.getSelectedClass()
                .orElseGet(() -> env.getCurClass().orElse(null));
        if (curClass == null) return;

        sortClasses(curClass);
        env.getWriteActions().run();
    }

    protected void sortClasses(PsiClass curClass) {
        PsiElement whiteSpace = PsiUtils.createWhiteSpace(env.getProject());

        PsiElement locateElement = Arrays.stream(curClass.getFields())
                .filter(PsiElement::isPhysical)
                .reduce((field1, field2) -> field2)
                .map(field -> (PsiElement) field)
                .orElseGet(curClass::getLBrace);

        Arrays.stream(curClass.getMethods())
                .filter(PsiElement::isPhysical)
                .sorted(getComparator())
                .forEachOrdered(method -> env.getWriteActions().add(() -> {
                    PsiElement e = curClass.addAfter(method.copy(), locateElement);
                    curClass.addBefore(whiteSpace, e);
                    method.delete();
                }));
    }

    private Comparator<PsiMethod> getComparator() {
        return Comparator.comparing(this::getMethodOrder).reversed()
                .thenComparing(PsiMethod::getName).reversed();
    }

    private int getMethodOrder(PsiMethod method) {
        int order = getOrder(method.getModifierList());
        if (method.hasAnnotation("org.springframework.scheduling.annotation.Scheduled")) {
            order += 50;
        }
        return order;
    }
}
