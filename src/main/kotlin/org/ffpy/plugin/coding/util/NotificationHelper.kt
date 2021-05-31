package org.ffpy.plugin.coding.util

import com.intellij.notification.NotificationType
import org.ffpy.plugin.coding.util.NotificationHelper
import cn.hutool.core.util.StrUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.Notifications
import org.ffpy.plugin.coding.constant.Constant
import java.util.*

/**
 * 通知工具类
 */
class NotificationHelper {
    private var title: String? = null
    private var content: String? = null
    private var type: NotificationType? = null
    private var listener: NotificationListener? = null

    /**
     * 设置标题
     *
     * @param title 标题
     * @return this
     */
    fun title(title: String): NotificationHelper {
        this.title = Objects.requireNonNull(title)
        return this
    }

    /**
     * 设置内容，支持HTML
     *
     * @param content 内容
     * @return this
     */
    fun content(content: String?): NotificationHelper {
        this.content = content ?: ""
        return this
    }

    /**
     * 设置内容，支持HTML
     *
     * @param content 内容
     * @return this
     */
    fun contentWithParam(content: String?, vararg param: Any?): NotificationHelper {
        this.content = StrUtil.format(content ?: "", *param)
        return this
    }

    /**
     * 设置类型
     *
     * @param type 类型
     * @return this
     */
    fun type(type: NotificationType?): NotificationHelper {
        this.type = Objects.requireNonNull(type)
        return this
    }

    /**
     * 设置监听器
     *
     * @param listener 监听器
     * @return this
     */
    fun listener(listener: NotificationListener?): NotificationHelper {
        this.listener = Objects.requireNonNull(listener)
        return this
    }

    /**
     * 显示通知
     */
    fun show() {
        Notifications.Bus.notify(Notification(Constant.GROUP_ID, title!!, content!!, type!!, listener))
    }

    companion object {
        private const val TITLE_ERROR = "错误"
        private const val TITLE_WARN = "警告"
        private const val TITLE_INFO = "信息"
        fun error(content: String?, vararg param: Any?): NotificationHelper {
            return NotificationHelper()
                .type(NotificationType.ERROR)
                .title(TITLE_ERROR)
                .contentWithParam(content, *param)
        }

        fun warn(content: String?, vararg param: Any?): NotificationHelper {
            return NotificationHelper()
                .type(NotificationType.WARNING)
                .title(TITLE_WARN)
                .contentWithParam(content, *param)
        }

        fun info(content: String?, vararg param: Any?): NotificationHelper {
            return NotificationHelper()
                .type(NotificationType.INFORMATION)
                .title(TITLE_INFO)
                .contentWithParam(content, *param)
        }
    }
}