package org.ffpy.plugin.coding.util

import cn.hutool.http.HttpUtil
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.ffpy.plugin.coding.service.SettingService
import org.json.JSONObject
import java.util.*

/**
 * 翻译工具类
 */
class TranslateHelper {
    private val appId: String
    private val secret: String

    constructor(appId: String, secret: String) {
        this.appId = appId
        this.secret = secret
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(secret)) {
            throw RuntimeException("百度翻译应用ID和密钥不能为空")
        }
    }

    constructor(project: Project) {
        val settingService = ServiceManager.getService(
            project, SettingService::class.java
        )
        appId = settingService.translateAppId ?: ""
        secret = settingService.translateSecret ?: ""
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(secret)) {
            throw RuntimeException("百度翻译应用ID和密钥不能为空")
        }
    }

    /**
     * 中文转英文
     *
     * @param text 中午字符串
     * @return 英文字符串
     */
    fun zh2En(text: String): String {
        return translate(text, "zh", "en")
    }

    /**
     * 翻译
     * 语言列表参考http://api.fanyi.baidu.com/api/trans/product/apidoc
     *
     * @param text 待翻译文本(UTF-8编码)
     * @param from 源语言，auto为自动检测
     * @param to   目标语言
     * @return 结果文本
     */
    fun translate(text: String, from: String, to: String): String {
        if (StringUtils.isEmpty(text)) {
            return text
        }
        require(text.toByteArray().size <= MAX_TEXT_BYTES_LENGTH) { "文本不能超过6000个字节" }
        val salt = salt
        val params: MutableMap<String, Any> = HashMap()
        params["q"] = text
        params["from"] = Objects.requireNonNull(from)
        params["to"] = Objects.requireNonNull(to)
        params["appid"] = appId
        params["salt"] = salt
        params["sign"] = getSign(text, salt)
        val response = HttpUtil.get(URL, params)
        if (response.isNotEmpty()) {
            return JSONObject(response).getJSONArray("trans_result")
                .getJSONObject(0).getString("dst")
        }
        throw Exception("翻译失败 response: $response")
    }

    private fun getSign(text: String, salt: String): String {
        return DigestUtils.md5Hex(appId + text + salt + secret)
    }

    private val salt: String
        get() = System.currentTimeMillis().toString()

    companion object {
        private const val URL = "http://api.fanyi.baidu.com/api/trans/vip/translate"
        private const val MAX_TEXT_BYTES_LENGTH = 6000
    }
}