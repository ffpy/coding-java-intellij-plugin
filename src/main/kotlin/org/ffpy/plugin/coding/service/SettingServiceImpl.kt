package org.ffpy.plugin.coding.service

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import org.ffpy.plugin.coding.constant.TemplateName
import java.io.IOException

class SettingServiceImpl(private val mProject: Project) : SettingService {
    private val projectProperties: PropertiesComponent
    private val applicationProperties: PropertiesComponent
    override var translateAppId: String?
        get() = applicationProperties.getValue(KEY_TRANSLATE_APP_ID)
        set(appId) {
            applicationProperties.setValue(KEY_TRANSLATE_APP_ID, appId)
        }
    override var translateSecret: String?
        get() = applicationProperties.getValue(KEY_TRANSLATE_SECRET)
        set(secret) {
            applicationProperties.setValue(KEY_TRANSLATE_SECRET, secret)
        }

    override fun getTemplate(name: TemplateName): String? {
        var template = projectProperties.getValue(getTemplateKey(name))
        if (template == null) {
            try {
                template = FileUtil.loadTextAndClose(javaClass.getResourceAsStream(name.path))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return template
    }

    override fun setTemplate(name: TemplateName, content: String?) {
        if (content == null) {
            projectProperties.unsetValue(getTemplateKey(name))
        } else {
            projectProperties.setValue(getTemplateKey(name), content)
        }
    }

    override fun reset() {
        TemplateName.values().forEach { setTemplate(it, null) }
    }

    private fun getTemplateKey(name: TemplateName): String {
        return KEY_TEMPLATE + name.fileName
    }

    companion object {
        private const val KEY_TEMPLATE = "template_"
        private const val KEY_TRANSLATE_APP_ID = "translate_app_id"
        private const val KEY_TRANSLATE_SECRET = "translate_secret"
    }

    init {
        projectProperties = PropertiesComponent.getInstance(mProject)
        applicationProperties = PropertiesComponent.getInstance()
    }
}