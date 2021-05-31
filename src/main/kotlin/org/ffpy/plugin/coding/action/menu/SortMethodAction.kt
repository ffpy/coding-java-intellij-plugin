package org.ffpy.plugin.coding.action.menu

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import org.ffpy.plugin.coding.action.BaseSortAction
import org.ffpy.plugin.coding.util.PsiUtils.createWhiteSpace
import java.util.*

/**
 * 方法排序
 */
class SortMethodAction : BaseSortAction() {

    override fun action() {
        val curClass = env.selectedClass ?: env.curClass ?: return
        sortClasses(curClass)
        env.writeActions.run()
    }

    protected fun sortClasses(curClass: PsiClass) {
        val whiteSpace: PsiElement = createWhiteSpace(env.project)
        val locateElement = Arrays.stream(curClass.fields)
            .filter { obj: PsiField -> obj.isPhysical }
            .reduce { _: PsiField?, field2: PsiField? -> field2 }
            .map { it as PsiElement }
            .orElseGet { curClass.lBrace }!!
        Arrays.stream(curClass.methods)
            .filter { obj: PsiMethod -> obj.isPhysical }
            .sorted(getComparator())
            .forEachOrdered { method: PsiMethod ->
                env.writeActions.add {
                    val e = curClass.addAfter(method.copy(), locateElement)
                    curClass.addBefore(whiteSpace, e)
                    method.delete()
                }
            }
    }

    private fun getComparator(): Comparator<PsiMethod?>? {
        return Comparator.comparing { method: PsiMethod ->
            getMethodOrder(
                method
            )
        }.reversed()
            .thenComparing { obj: PsiMethod -> obj.name }.reversed()
    }

    private fun getMethodOrder(method: PsiMethod): Int {
        var order = getOrder(method.modifierList)
        if (method.hasAnnotation("org.springframework.scheduling.annotation.Scheduled")) {
            order += 50
        }
        return order
    }
}