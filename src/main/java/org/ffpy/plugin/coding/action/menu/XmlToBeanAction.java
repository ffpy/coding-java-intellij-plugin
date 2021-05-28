package org.ffpy.plugin.coding.action.menu;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.ffpy.plugin.coding.action.BaseAction;
import org.ffpy.plugin.coding.constant.CommentPosition;
import org.ffpy.plugin.coding.constant.TemplateName;
import org.ffpy.plugin.coding.ui.form.XmlToBeanForm;
import org.ffpy.plugin.coding.util.CopyPasteUtils;
import org.ffpy.plugin.coding.util.FileUtils;
import org.ffpy.plugin.coding.util.MyStringUtils;
import org.ffpy.plugin.coding.util.StreamUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 根据XML生成对应的Bean
 */
@Slf4j
public class XmlToBeanAction extends BaseAction implements XmlToBeanForm.Action {

    @Override
    public void action() throws Exception {
        XmlToBeanForm form = new XmlToBeanForm(getCopyText().orElse(null));
        form.setAction(this);
        form.pack();
        form.setLocationRelativeTo(null);
        form.setVisible(true);
    }

    @Override
    public void onOk(Document doc, String packageName, CommentPosition position) {
        parseElement(doc.getRootElement(), getDirectory(packageName), position);
        env.getWriteActions().run();
    }

    private FieldData parseElement(Element el, PsiDirectory directory, CommentPosition position) {
        String comment = el.isRootElement() ? null : getComment(el, position);

        if (el.isTextOnly()) {
            return new FieldData(normalFieldName(el.getName()), "String", el.getName(), comment);
        } else {
            String className = normalClassName(el.getName());

            List<Element> elements = el.elements();
            Set<String> listElements = getListElements(elements);

            List<FieldData> fields = elements.stream()
                    .filter(StreamUtils.distinct(Node::getName))
                    .map(e -> parseElement(e, directory, position))
                    .map(field -> processList(listElements, field))
                    .collect(Collectors.toList());

            Map<String, Object> params = new HashMap<>(8);
            params.put("className", className);
            params.put("elementName", el.getName());
            params.put("isRoot", el.isRootElement());
            params.put("fields", fields);
            params.put("hasSingle", fields.size() != listElements.size());
            params.put("hasList", !listElements.isEmpty());

            env.getWriteActions().add(() -> {
                PsiFile psiFile = env.createJavaFile(TemplateName.XML_TO_BEAN, className, params);
                FileUtils.addIfAbsent(directory, psiFile);
                if (el.isRootElement()) {
                    FileUtils.navigateFile(env.getProject(), directory, psiFile.getName());
                }
            });

            return new FieldData(normalFieldName(el.getName()), className, el.getName(), comment);
        }
    }

    private FieldData processList(Set<String> listElements, FieldData field) {
        field.setIsList(listElements.contains(field.getElementName()));
        if (field.getIsList()) {
            field.setType(StrUtil.format("List<{}>", field.getType()));

            if (field.getName().endsWith("s")) {
                field.setName(field.getName() + "List");
            } else {
                field.setName(field.getName() + "s");
            }
        }
        return field;
    }

    /**
     * 获取是List类型的标签名
     *
     * @param elements 标签列表
     * @return 是List类型的标签名集合
     */
    private Set<String> getListElements(List<Element> elements) {
        Map<String, Integer> elementCounter = new HashMap<>();
        elements.stream().map(Node::getName)
                .forEach(name -> elementCounter.put(name,
                        elementCounter.getOrDefault(name, 0) + 1));
        elementCounter.values().removeIf(count -> count <= 1);
        return elementCounter.keySet();
    }

    private String getComment(Element el, CommentPosition position) {
        return position.findComment(el).map(Node::getText).orElse(null);
    }

    private PsiDirectory getDirectory(String packageName) {
        try {
            return WriteAction.computeAndWait(() -> env.findOrCreateDirectoryByPackageName(packageName));
        } catch (IOException e) {
            throw new RuntimeException("生成包名失败: " + e.getMessage());
        }
    }

    private Optional<String> getCopyText() {
        return CopyPasteUtils.getString().filter(it -> it.startsWith("<"));
    }

    private String normalFieldName(String name) {
        if (name.length() < 2) return name.toLowerCase();

        name = MyStringUtils.underScoreCase2CamelCase(name);
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private String normalClassName(String name) {
        if (name.length() < 2) return name.toLowerCase();

        name = MyStringUtils.underScoreCase2CamelCase(name);
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Data
    public static class FieldData {
        /** 字段名 */
        private String name;

        /** 字段类型 */
        private String type;

        /** 标签名 */
        private String elementName;

        /** 注释 */
        private String comment;

        /** 是否是List类型 */
        private Boolean isList = false;

        public FieldData(String name, String type, String elementName, String comment) {
            this.name = name;
            this.type = type;
            this.elementName = elementName;
            this.comment = comment;
        }
    }
}
