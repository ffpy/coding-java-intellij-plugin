package org.ffpy.plugin.coding.service

import com.sun.istack.Nullable
import org.ffpy.plugin.coding.constant.TemplateName

interface SettingService {
    var translateAppId: String?
    var translateSecret: String?
    fun getTemplate(name: TemplateName): String?
    fun setTemplate(name: TemplateName, @Nullable content: String?)
    fun reset()
}