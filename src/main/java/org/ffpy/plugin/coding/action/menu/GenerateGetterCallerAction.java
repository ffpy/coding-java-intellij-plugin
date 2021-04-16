package org.ffpy.plugin.coding.action.menu;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.ffpy.plugin.coding.util.PsiUtils;

import java.util.stream.Stream;

/**
 * 生成Getter调用
 */
public class GenerateGetterCallerAction extends BaseSetterGetterCallerAction {

    @Override
    protected Stream<PsiMethod> getMethodStream(PsiClass targetClass) {
        return PsiUtils.getAllGetter(targetClass);
    }
}
