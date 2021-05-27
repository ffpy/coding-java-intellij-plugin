package org.ffpy.plugin.coding.util;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.ui.Messages;

public class MsgUtils {

    private static final String TITLE_INFO = "提示";
    private static final String TITLE_WARN = "警告";
    private static final String TITLE_ERROR = "错误";

    public static void info(String message, Object... param) {
        Messages.showInfoMessage(StrUtil.format(message, param), TITLE_INFO);
    }

    public static void warn(String message, Object... param) {
        Messages.showWarningDialog(StrUtil.format(message, param), TITLE_WARN);
    }

    public static void error(String message, Object... param) {
        Messages.showErrorDialog(StrUtil.format(message, param), TITLE_ERROR);
    }
}
