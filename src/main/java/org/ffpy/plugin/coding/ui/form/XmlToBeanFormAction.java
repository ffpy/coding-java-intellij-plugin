package org.ffpy.plugin.coding.ui.form;

import org.dom4j.Document;
import org.ffpy.plugin.coding.constant.CommentPosition;

public interface XmlToBeanFormAction {
    /**
     * 按下了确定按钮
     *
     * @param doc         XML文档
     * @param packageName 包名
     * @param position    注释位置
     */
    void onOk(Document doc, String packageName, CommentPosition position);
}
