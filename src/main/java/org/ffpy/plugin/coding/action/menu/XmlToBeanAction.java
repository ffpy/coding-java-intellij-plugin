package org.ffpy.plugin.coding.action.menu;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.ffpy.plugin.coding.action.BaseAction;
import org.ffpy.plugin.coding.constant.CommentPosition;
import org.ffpy.plugin.coding.constant.TemplateName;
import org.ffpy.plugin.coding.ui.form.XmlToBeanForm;
import org.ffpy.plugin.coding.util.CopyPasteUtils;
import org.ffpy.plugin.coding.util.FileUtils;
import org.ffpy.plugin.coding.util.NotificationHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 根据XML生成对应的Bean
 */
@Slf4j
public class XmlToBeanAction extends BaseAction implements XmlToBeanForm.Action {

    // el.selectNodes("./preceding-sibling::comment()[1]")
    // el.selectNodes("./following-sibling::comment()[1]")

    @Override
    public void action() throws Exception {
        XmlToBeanForm form = new XmlToBeanForm(getCopyText().orElse(null));
        form.setAction(this);
        form.setLocationRelativeTo(null);
        form.setVisible(true);
    }

    @Override
    public void onOk(Document doc, String packageName, CommentPosition position) {
        PsiDirectory directory = getDirectory(packageName);
        if (directory == null) return;

        parseElement(doc.getRootElement(), directory);

        env.getWriteActions().run();
    }

    private FieldData parseElement(Element el, PsiDirectory directory) {
        PsiElementFactory factory = env.getElementFactory();
        if (el.isTextOnly()) {
            return new FieldData(normalFieldName(el.getName()), "String", el.getName());
        } else {
            PsiClass psiClass = factory.createClass(el.getName());

            List<FieldData> fields = el.elements().stream()
                    .map(e -> parseElement(e, directory))
                    .collect(Collectors.toList());

            Map<String, Object> params = new HashMap<>(8);
            params.put("className", psiClass.getName());
            params.put("fields", fields);

            env.getWriteActions().add(() -> {
                PsiFile psiFile = env.createJavaFile(TemplateName.XML_TO_BEAN, psiClass.getName(), params);
                FileUtils.addIfAbsent(directory, psiFile);
            });

            return new FieldData(normalFieldName(el.getName()), psiClass.getName(), el.getName());
        }
    }

    private PsiDirectory getDirectory(String packageName) {
        try {
            return env.findOrCreateDirectoryByPackageName(packageName);
        } catch (IOException e) {
            NotificationHelper.error("生成包名失败: {}", e.getMessage());
            return null;
        }
    }

    private Optional<String> getCopyText() {
        return CopyPasteUtils.getString().filter(it -> it.startsWith("<"));
    }

    private String normalFieldName(String name) {
        StringBuilder sb = new StringBuilder();

        return name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldData {
        /** 字段名 */
        private String name;

        /** 字段类型 */
        private String type;

        /** 标签名 */
        private String elementName;
    }
}
