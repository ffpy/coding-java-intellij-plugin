package org.ffpy.plugin.coding.action.menu;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.ffpy.plugin.coding.action.BaseReplaceAction;
import org.ffpy.plugin.coding.util.MyStringUtils;

/**
 * 驼峰-下划线互转
 */
public class CaseFormatAction extends BaseReplaceAction {

    @Override
    protected String replace(AnActionEvent e, String text) throws Exception {
        return text.contains("_") ? MyStringUtils.underScoreCase2CamelCase(text) :
                MyStringUtils.camelCase2UnderScoreCase(text);
    }
}
