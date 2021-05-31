package org.ffpy.plugin.coding.action.generate

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.ffpy.plugin.coding.action.BaseAction
import org.ffpy.plugin.coding.constant.AnnotationNames
import org.ffpy.plugin.coding.util.ActionShowHelper
import org.ffpy.plugin.coding.util.IndexUtils.getClassByQualifiedName
import org.ffpy.plugin.coding.util.PsiUtils.getAllGetterName
import org.ffpy.plugin.coding.util.PsiUtils.getAllSetterName
import org.ffpy.plugin.coding.util.PsiUtils.getAnnotationValue
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.collections.LinkedHashSet

/**
 * Mapping自动添加ignore
 */
class AddMappingIgnoreAction : BaseAction() {

    override fun action() {
        val curMethodModifierList = getCurMethodModifierList() ?: return

        getIgnoreFields().forEach(Consumer { field: String? ->
            val annotation = env.elementFactory.createAnnotationFromText(
                "@Mapping(target = \"$field\", ignore = true)", null
            )
            env.writeActions.add { curMethodModifierList.add(annotation) }
        })

        env.writeActions.run()
    }

    override fun update(e: AnActionEvent) {
        ActionShowHelper.of(e)
            .classWithAnnotation(AnnotationNames.MAPPER)
            .elementType(PsiMethod::class.java)
            .update()
    }

    private fun getCurMethodModifierList(): PsiModifierList? {
        return env.selectedMethod?.let { PsiTreeUtil.getChildOfType(it, PsiModifierList::class.java) }
    }

    /**
     * 获取返回类的字段列表
     */
    @Suppress("USELESS_CAST")
    private fun getReturnFields(): Set<String?> {
        val selectedMethod = env.selectedMethod ?: return emptySet<String>()
        return Optional.ofNullable(selectedMethod.returnType)
            .flatMap { type: PsiType ->
                Optional.ofNullable(
                    getClassByQualifiedName(env.project, type.canonicalText)
                )
            }
            .map<List<String?>> { psiClass: PsiClass ->
                val list = getAllSetterName(psiClass)
                    .collect(Collectors.toList())
                // 处理XXXRecord的valueXX方法
                list.addAll(
                    Arrays.stream(psiClass.allMethods)
                        .filter { method: PsiMethod ->
                            val modifierList = method.modifierList
                            modifierList.hasModifierProperty(PsiModifier.PUBLIC) &&
                                    !modifierList.hasModifierProperty(PsiModifier.STATIC)
                        }
                        .filter { method: PsiMethod ->
                            method.name.matches(Regex("value\\d+"))
                        }
                        .map { obj: PsiMethod -> obj.name }
                        .collect(Collectors.toList())
                )
                list
            } // 保持顺序
            .map { LinkedHashSet(it) as Set<String?> }
            .orElseGet { emptySet() }
    }

    /**
     * 获取参数字段列表
     */
    private fun getParameterArgs(): Set<String?>? {
        val selectedMethod: PsiMethod = env.selectedMethod ?: return emptySet<String>()
        val args: MutableSet<String?> = LinkedHashSet()
        for (parameter in selectedMethod.parameterList.parameters) {
            val parameterClass = getClassByQualifiedName(
                env.project,
                parameter.type.canonicalText
            )
            val isCustomClass = Optional.ofNullable(parameterClass)
                .map { obj: PsiClass -> obj.qualifiedName }
                .map { name: String? ->
                    !name!!.startsWith(
                        "java.lang"
                    )
                }
                .orElse(false)
            if (isCustomClass) {
                args.addAll(
                    getAllGetterName(parameterClass)
                        .collect(Collectors.toSet())
                )
            } else {
                args.add(parameter.name)
            }
        }
        return args
    }

    /**
     * 获取要忽略的字段列表
     */
    private fun getIgnoreFields(): Set<String?> {
        val selectedMethod: PsiMethod = env.selectedMethod ?: return emptySet<String>()
        val set: MutableSet<String?> = LinkedHashSet(getReturnFields())
        set.removeAll(getParameterArgs()!!)
        Arrays.stream(selectedMethod.annotations)
            .filter { anno: PsiAnnotation -> AnnotationNames.MAPPING == anno.qualifiedName }
            .map { anno: PsiAnnotation? ->
                getAnnotationValue(
                    anno, "target",
                    String::class.java
                )
            }
            .filter { obj: String? -> Objects.nonNull(obj) }
            .forEach { o: String? -> set.remove(o) }
        return set
    }
}