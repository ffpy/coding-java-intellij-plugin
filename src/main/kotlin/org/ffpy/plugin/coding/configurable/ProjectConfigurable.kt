package org.ffpy.plugin.coding.configurable

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.ffpy.plugin.coding.constant.TemplateName
import org.ffpy.plugin.coding.service.SettingService
import org.ffpy.plugin.coding.ui.form.ConfigurationForm
import java.util.*
import java.util.stream.Collectors
import javax.swing.JComponent

class ProjectConfigurable(project: Project?) : SearchableConfigurable {

    private val form: ConfigurationForm
    private val settingService = ServiceManager.getService(project!!, SettingService::class.java)
    private var templateMap: Map<TemplateName, String>? = null
    private var translateAppId: String
    private var translateSecret: String
    private fun initTemplateMap() {
        templateMap = Arrays.stream(TemplateName.values())
            .map { name: TemplateName -> loadTemplate(name) }
            .filter { Objects.nonNull(it) }
            .collect(
                Collectors.toMap(
                    { it!!.name },
                    { it!!.content },
                    { _: String?, _: String? -> throw IllegalStateException("Duplicate key") },
                    { TreeMap() })
            )
    }

    override fun getId() = "org.ffpy.plugin.coding"

    override fun getDisplayName() = "Coding"

    override fun createComponent(): JComponent? {
        return form.mainPanel
    }

    override fun isModified(): Boolean {
        return form.templateMap != templateMap ||
                !form.translateAppIdField.getText().equals(translateAppId) ||
                !form.translateSecretField.getText().equals(translateSecret)
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        applyTemplate()
        applyTranslateAppId()
        applyTranslateSecret()
    }

    private fun applyTemplate() {
        val templateMap: Map<TemplateName, String> = form.getTemplateMap()
        templateMap.forEach { (name: TemplateName?, content: String?) ->
            settingService.setTemplate(
                name,
                content
            )
        }
        this.templateMap = templateMap
    }

    private fun applyTranslateAppId() {
        val translateAppId: String = form.getTranslateAppIdField().getText()
        settingService.translateAppId = translateAppId
        this.translateAppId = translateAppId
    }

    private fun applyTranslateSecret() {
        val translateSecret: String = form.translateSecretField.getText()
        settingService.translateSecret = translateSecret
        this.translateSecret = translateSecret
    }

    override fun reset() {
        form.templateMap = templateMap
        form.translateAppIdField.setText(translateAppId)
        form.translateSecretField.setText(translateSecret)
    }

    override fun disposeUIResources() {
        form.dispose()
    }

    private fun loadTemplate(name: TemplateName): Item<String>? {
        return settingService.getTemplate(name)?.let {
            Item(name, it)
        }
    }

    private data class Item<T>(
        val name: TemplateName? = null,
        val content: T? = null
    )

    init {
        initTemplateMap()
        form = ConfigurationForm(templateMap)
        translateAppId = settingService.translateAppId ?: ""
        translateSecret = settingService.translateSecret ?: ""
        form.onReset {
            if (Messages.showYesNoDialog(
                    "确认恢复默认设置？", "提示",
                    "确定", "取消", null
                ) == Messages.YES
            ) {
                settingService.reset()
                initTemplateMap()
                reset()
            }
        }
    }
}