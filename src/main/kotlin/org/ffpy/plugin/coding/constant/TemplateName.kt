package org.ffpy.plugin.coding.constant

/**
 * 模板名称
 */
enum class TemplateName(val fileName: String) {
    ENUM_CODE("EnumCode.vm"),
    XML_TO_BEAN("XmlToBean.vm");

    val path: String
        get() = PATH_TEMPLATE + fileName

    companion object {
        const val PATH_TEMPLATE = "/template/"
    }
}