package org.ffpy.plugin.coding.action.menu

import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.ffpy.plugin.coding.action.BaseSortAction
import org.ffpy.plugin.coding.util.PsiUtils.createWhiteSpace
import java.util.*
import java.util.stream.Collectors

/**
 * 字段排序
 */
class SortFieldAction : BaseSortAction() {

    override fun action() {
        val curClass: PsiClass = env.selectedClass ?: env.curClass ?: return
        if (curClass.isEnum) {
            sortEnum(curClass)
        } else {
            sortClass(curClass)
        }
        env.writeActions.run()
    }

    /**
     * 枚举类排序枚举值
     */
    private fun sortEnum(curClass: PsiClass) {
        val originalConstants = PsiTreeUtil.findChildrenOfType(
            curClass, PsiEnumConstant::class.java
        )
        val sortedConstants = originalConstants.stream()
            .sorted(Comparator.comparing { obj: PsiEnumConstant -> obj.name })
            .map { constant: PsiEnumConstant -> constant.copy() as PsiEnumConstant }
            .collect(Collectors.toList())
        val comment: PsiComment = env.elementFactory.createCommentFromText("/**/", null)
        val whiteSpace = createWhiteSpace(env.project)
        val newLine = createWhiteSpace(env.project, 1)
        env.writeActions.add {
            var lastGroupName: String? = null
            var isFirst = true
            val it = sortedConstants.iterator()
            for (originalConstant in originalConstants) {
                val curConstant = originalConstant.replace(it.next()) as PsiEnumConstant

                // 删除原换行
                val prevElement = curConstant.prevSibling
                if (prevElement is PsiWhiteSpace && prevElement.getText() != "\n") {
                    prevElement.replace(newLine.copy())
                }

                // 分组换行
                val groupName = getEnumGroupName(curConstant)
                if (groupName != lastGroupName) {
                    if (!isFirst) {
                        curConstant.addBefore(comment.copy(), curConstant.firstChild)
                            .replace(whiteSpace.copy())
                    }
                    lastGroupName = groupName
                    isFirst = false
                }
            }
        }
    }

    /**
     * 普通类排序字段
     */
    private fun sortClass(curClass: PsiClass) {
        val whiteSpace: PsiElement = createWhiteSpace(env.project)
        val locateElement = curClass.lBrace
        Arrays.stream(curClass.fields)
            .sorted(
                Comparator.comparing { field: PsiField ->
                    getFieldOrder(
                        field
                    )
                }.reversed()
                    .thenComparing { obj: PsiField -> obj.name }.reversed()
            )
            .filter { obj: PsiField -> obj.isPhysical }
            .forEachOrdered { field: PsiField ->
                env.writeActions.add {
                    curClass.addAfter(field.copy(), locateElement)
                    curClass.addAfter(whiteSpace.copy(), locateElement)
                    field.delete()
                }
            }
    }

    private fun getEnumGroupName(constant: PsiEnumConstant): String {
        val name = constant.name
        val i = name.indexOf("__")
        return if (i == -1) name else name.substring(0, i)
    }

    private fun getFieldOrder(field: PsiField): Int {
        return getOrder(field.modifierList)
    }
}