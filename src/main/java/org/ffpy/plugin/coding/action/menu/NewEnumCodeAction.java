package org.ffpy.plugin.coding.action.menu;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ffpy.plugin.coding.action.BaseAction;
import org.ffpy.plugin.coding.constant.TemplateName;
import org.ffpy.plugin.coding.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 生成EnumCode
 */
@Slf4j
public class NewEnumCodeAction extends BaseAction {

    private static final Predicate<String> PREDICATE_UPDATE = Pattern.compile(
            "\\s*`\\w+`\\s+(TINY)?INT\\(\\d*\\)\\s+.*COMMENT\\s+'.+[:：].*'.*",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE).asPredicate();

    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile(
            "CREATE\\s+TABLE\\s+`(\\w+)`", Pattern.CASE_INSENSITIVE);

    private static final Predicate<String> PREDICATE_CREATE_TABLE = Pattern.compile(
            ".*CREATE\\s+TABLE.*", Pattern.CASE_INSENSITIVE).asPredicate();

    private static final Pattern PATTERN_COLUMN_NAME = Pattern.compile(
            "^\\s*`(\\w+)`");

    private static final Pattern PATTERN_COMMENT = Pattern.compile(
            "COMMENT\\s+'.*[:：](.+)'", Pattern.CASE_INSENSITIVE);

    @Override
    public void action() throws Exception {
        PsiDirectory directory = getDirectory()
                .orElseThrow(() -> new RuntimeException("找不到constant文件夹"));

        PsiFile file = createFile(directory);
        if (file == null) return;

        env.getWriteActions().add(() -> {
            FileUtils.addIfAbsent(directory, file);
            FileUtils.navigateFile(env.getProject(), directory, file.getName());
        }).run();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ActionShowHelper.of(e)
                .fileNameMatch(".*\\.sql")
                .and(() -> PREDICATE_UPDATE.test(getLineText(e)))
                .update();
    }

    /**
     * 获取字段名
     *
     * @param lineText 当前行
     */
    private String getColumnName(String lineText) {
        Matcher matcher = PATTERN_COLUMN_NAME.matcher(lineText);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * 获取注释
     */
    private String getComment(String lineText) {
        Matcher matcher = PATTERN_COMMENT.matcher(lineText);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * 获取类名
     *
     * @param lineText 当前行
     */
    private String getClassName(String lineText) {
        return MyStringUtils.toTitle(getTableName().toLowerCase()) +
                MyStringUtils.toTitle(getColumnName(lineText).toLowerCase());
    }

    /**
     * 获取模板参数
     *
     * @param lineText  当前行
     * @param className 类名
     */
    private Map<String, Object> getParams(String lineText, String className) {
        Map<String, Object> params = new HashMap<>();
        params.put("className", className);
        params.put("items", getItems(getComment(lineText)));
        return params;
    }

    /**
     * 获取项目列表
     */
    private List<Item> getItems(String comment) {
        List<Item> items = Arrays.stream(StringUtils.split(comment, ",，"))
                .map(item -> {
                    String[] split = StringUtils.split(item, "-");
                    return split.length == 2 ? new Item(split[0], split[1], split[1]) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 翻译
        boolean needTranslate = false;
        // 如果没有中文则不需要翻译
        for (Item item : items) {
            if (MyStringUtils.hasChinese(item.getName())) {
                needTranslate = true;
                break;
            }
        }

        if (needTranslate) {
            try {
                String text = items.stream().map(Item::getName).reduce((s1, s2) -> s1 + "。" + s2)
                        .orElse("");
                String result = env.getTranslateHelper().zh2En(text);
                String[] names = StringUtils.split(result, '.');
                for (int i = 0; i < names.length && i < items.size(); i++) {
                    String name = names[i];
                    if (StringUtils.isNotEmpty(name)) {
                        items.get(i).setName(name.trim().toUpperCase().replaceAll("[^\\w]+", "_"));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return items;
    }

    /**
     * 获取表名
     */
    private String getTableName() {
        Editor editor = env.getEditor().orElse(null);
        if (editor == null) return "";

        CaretModel caretModel = editor.getCaretModel();
        int line = caretModel.getLogicalPosition().line;
        Document document = editor.getDocument();
        while (line > 0) {
            String text = EditorUtils.getLineText(document, --line);
            if (text == null) {
                return "";
            }
            if (PREDICATE_CREATE_TABLE.test(text)) {
                Matcher matcher = PATTERN_CREATE_TABLE.matcher(text);
                return matcher.find() ? matcher.group(1) : "";
            }
        }
        return "";
    }

    /**
     * 生成Java文件
     *
     * @param directory 文件夹
     */
    private PsiFile createFile(PsiDirectory directory) {
        String lineText = getLineText(env.getEvent());

        String className = getClassName(lineText);
        String filename = className;
        String path = filename + ".java";
        if (directory.findFile(path) != null) {
            NotificationHelper.info("{}已存在", filename).show();
            FileUtils.navigateFile(env.getProject(), directory, path);
            return null;
        }
        return env.createJavaFile(TemplateName.ENUM_CODE, filename,
                getParams(lineText, className));
    }

    /**
     * 获取文件夹
     */
    private Optional<PsiDirectory> getDirectory() {
        return env.getPackageFile()
                .map(file -> file.findFileByRelativePath("/constant"))
                .map(it -> env.getDirectoryFactory().createDirectory(it));
    }

    /**
     * 获取当前行内容
     */
    private String getLineText(@NotNull AnActionEvent e) {
        return Optional.ofNullable(e.getData(LangDataKeys.EDITOR))
                .map(EditorUtils::getCurLineText)
                .orElse("");
    }

    @Data
    @AllArgsConstructor
    public static class Item {

        /** 码值 */
        private String code;

        /** 字段名 */
        private String name;

        /** 注释 */
        private String comment;
    }
}
