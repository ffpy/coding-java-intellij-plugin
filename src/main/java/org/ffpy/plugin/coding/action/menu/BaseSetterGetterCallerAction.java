package org.ffpy.plugin.coding.action.menu;

import cn.hutool.core.util.StrUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ffpy.plugin.coding.action.BaseAction;
import org.ffpy.plugin.coding.util.ActionShowHelper;
import org.ffpy.plugin.coding.util.EditorUtils;
import org.ffpy.plugin.coding.util.IndexUtils;
import org.ffpy.plugin.coding.util.NotificationHelper;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 生成Getter、Setter调用的基类
 */
@Slf4j
public abstract class BaseSetterGetterCallerAction extends BaseAction {

    /** 缩进匹配正则 */
    private static final Pattern INDENT_PATTERN = Pattern.compile("(\\s*).*");

    /**
     * 获取调用方法流
     *
     * @param targetClass 目标Class
     * @return 调用方法流
     */
    protected abstract Stream<PsiMethod> getMethodStream(PsiClass targetClass);

    @Override
    public void action() throws Exception {
        Editor editor = env.getEditor().orElse(null);
        if (editor == null) {
            NotificationHelper.error("找不到编辑器").show();
            return;
        }

        String callers = getCallers();
        if (StringUtils.isNotBlank(callers)) {
            EditorUtils.insertToNextLine(editor, "\n" + callers);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Transferable contents = CopyPasteManager.getInstance().getContents();
        //noinspection ConstantConditions
        ActionShowHelper.of(e)
                .and(() -> contents != null)
                .and(() -> contents.isDataFlavorSupported(DataFlavor.stringFlavor))
                .and(() -> Optional.ofNullable(e.getData(LangDataKeys.EDITOR))
                        .map(Editor::getSelectionModel)
                        .map(SelectionModel::hasSelection)
                        .orElse(false))
                .update();
    }

    /**
     * 获取调用字符串
     */
    private String getCallers() {
        PsiClass targetClass = getTargetClass().orElse(null);
        if (targetClass == null) return "";

        String varName = getVarName();
        String callers = getMethodStream(targetClass)
                .map(method -> StrUtil.format("{}.{}();", varName, method.getName()))
                .distinct()
                .map(call -> getIndent() + call + "\n")
                .collect(Collectors.joining());
        return callers.endsWith("\n") ?
                callers.substring(0, callers.length() - "\n".length()) : callers;
    }

    /**
     * 获取目标Class
     */
    private Optional<PsiClass> getTargetClass() {
        Project project = env.getProject();
        return Optional.ofNullable(CopyPasteManager.getInstance().getContents())
                .filter(contents -> contents.isDataFlavorSupported(DataFlavor.stringFlavor))
                .map(contents -> {
                    try {
                        return (String) contents.getTransferData(DataFlavor.stringFlavor);
                    } catch (UnsupportedFlavorException | IOException e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .map(name -> name.contains(".") ? IndexUtils.getClassByQualifiedName(project, name) :
                        IndexUtils.getClassByShortName(project, name));
    }

    /**
     * 获取变量名
     */
    private String getVarName() {
        return env.getEditor()
                .map(Editor::getSelectionModel)
                .filter(SelectionModel::hasSelection)
                .map(SelectionModel::getSelectedText)
                .orElse(null);
    }

    /**
     * 获取缩进字符串
     */
    private String getIndent() {
        Editor editor = env.getEditor().orElse(null);
        if (editor == null) return "";

        String indent = null;
        Matcher matcher = INDENT_PATTERN.matcher(EditorUtils.getCurLineText(editor));
        if (matcher.find()) {
            indent = matcher.group(1);
        }
        if (indent == null) {
            indent = "";
        }
        return indent;
    }
}
