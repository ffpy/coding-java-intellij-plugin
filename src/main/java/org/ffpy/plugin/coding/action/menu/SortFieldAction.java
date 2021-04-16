package org.ffpy.plugin.coding.action.menu;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.ffpy.plugin.coding.util.PsiUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 字段排序
 */
public class SortFieldAction extends BaseSortAction {

    @Override
    public void action() throws Exception {
        PsiClass curClass = env.getSelectedClass()
                .orElseGet(() -> env.getCurClass().orElse(null));
        if (curClass == null) return;

        if (curClass.isEnum()) {
            sortEnum(curClass);
        } else {
            sortClass(curClass);
        }
        env.getWriteActions().run();
    }

    /**
     * 枚举类排序枚举值
     */
    private void sortEnum(PsiClass curClass) {
        Collection<PsiEnumConstant> originalConstants = PsiTreeUtil.findChildrenOfType(
                curClass, PsiEnumConstant.class);
        List<PsiEnumConstant> sortedConstants = originalConstants.stream()
                .sorted(Comparator.comparing(PsiField::getName))
                .map(constant -> (PsiEnumConstant) constant.copy())
                .collect(Collectors.toList());
        PsiComment comment = env.getElementFactory().createCommentFromText("/**/", null);
        PsiWhiteSpace whiteSpace = PsiUtils.createWhiteSpace(env.getProject());
        PsiWhiteSpace newLine = PsiUtils.createWhiteSpace(env.getProject(), 1);

        env.getWriteActions().add(() -> {
            String lastGroupName = null;
            boolean isFirst = true;
            Iterator<PsiEnumConstant> it = sortedConstants.iterator();

            for (PsiEnumConstant originalConstant : originalConstants) {
                PsiEnumConstant curConstant = (PsiEnumConstant) originalConstant.replace(it.next());

                // 删除原换行
                PsiElement prevElement = curConstant.getPrevSibling();
                if (prevElement instanceof PsiWhiteSpace && !prevElement.getText().equals("\n")) {
                    prevElement.replace(newLine.copy());
                }

                // 分组换行
                String groupName = getEnumGroupName(curConstant);
                if (!Objects.equals(groupName, lastGroupName)) {
                    if (!isFirst) {
                        curConstant.addBefore(comment.copy(), curConstant.getFirstChild())
                                .replace(whiteSpace.copy());
                    }

                    lastGroupName = groupName;
                    isFirst = false;
                }
            }
        });
    }

    /**
     * 普通类排序字段
     */
    private void sortClass(PsiClass curClass) {
        PsiElement whiteSpace = PsiUtils.createWhiteSpace(env.getProject());
        PsiElement locateElement = curClass.getLBrace();

        Arrays.stream(curClass.getFields())
                .sorted(Comparator.comparing(this::getFieldOrder).reversed()
                        .thenComparing(PsiField::getName).reversed())
                .filter(PsiElement::isPhysical)
                .forEachOrdered(field -> env.getWriteActions().add(() -> {
                    curClass.addAfter(field.copy(), locateElement);
                    curClass.addAfter(whiteSpace.copy(), locateElement);
                    field.delete();
                }));
    }

    private String getEnumGroupName(PsiEnumConstant constant) {
        String name = constant.getName();
        int i = name.indexOf("__");
        return i == -1 ? name : name.substring(0, i);
    }

    private int getFieldOrder(PsiField field) {
        return getOrder(field.getModifierList());
    }
}
