package org.ffpy.plugin.coding;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.ffpy.plugin.coding.service.SettingService;
import org.ffpy.plugin.coding.util.IndexUtils;
import org.ffpy.plugin.coding.util.ProjectUtils;
import org.ffpy.plugin.coding.util.PsiUtils;
import org.ffpy.plugin.coding.util.WriteActions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ActionEnv {

    /** 当前Event */
    @Getter
    @NonNull
    private final AnActionEvent event;

    /** 对象缓存 */
    private final Map<String, Object> cacheMap = new HashMap<>();

    /**
     * 获取当前打开的文件
     */
    public Optional<PsiFile> getCurFile() {
        return cache("getCurFile", () ->
                Optional.ofNullable(event.getData(LangDataKeys.PSI_FILE)));
    }

    /**
     * 获取当前打开的Java文件
     */
    public Optional<PsiJavaFile> getCurJavaFile() {
        return cache("getCurJavaFile", () ->
                getCurFile().map(it -> (PsiJavaFile) it));
    }

    /**
     * 获取当前工程
     */
    public Project getProject() {
        return cache("getProject", () ->
                Optional.ofNullable(event.getProject())
                        .orElseThrow(() -> new RuntimeException("getProject fail.")));
    }

    /**
     * 获取PSI元素工厂
     */
    public PsiElementFactory getElementFactory() {
        return cache("getElementFactory", () -> JavaPsiFacade.getElementFactory(getProject()));
    }

    /**
     * 获取WriteActions
     */
    public WriteActions getWriteActions() {
        return cache("getWriteActions", () -> new WriteActions(getProject()));
    }

    /**
     * 获取目录工厂
     */
    public PsiDirectoryFactory getDirectoryFactory() {
        return cache("getDirectoryFactory", () -> PsiDirectoryFactory.getInstance(getProject()));
    }

    /**
     * 获取文件工程
     */
    public PsiFileFactory getFileFactory() {
        return cache("getFileFactory", () -> PsiFileFactory.getInstance(getProject()));
    }

    /**
     * 获取当前项目的根目录
     */
    public VirtualFile getProjectRootFile() {
        return cache("getProjectRootFile", () ->
                Optional.ofNullable(ProjectUtils.getRootFile(getProject()))
                        .orElseThrow(() -> new RuntimeException("getProjectRootFile fail.")));
    }

    /**
     * 获取设置服务
     */
    public SettingService getSettingService() {
        return ServiceManager.getService(getProject(), SettingService.class);
    }

    /**
     * 获取编辑器实例
     */
    public Optional<Editor> getEditor() {
        return cache("getEditor", () ->
                Optional.ofNullable(event.getData(LangDataKeys.HOST_EDITOR)));
    }

    /**
     * 获取当前光标所指元素
     */
    public Optional<PsiElement> getCurElement() {
        return cache("getCurElement", () ->
                Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT)));
    }

    /**
     * 获取PSI管理器
     */
    public PsiManager getPsiManager() {
        return cache("getPsiManager", () ->
                PsiManager.getInstance(getProject()));
    }

    /**
     * 获取当前打开的Java文件的顶层类
     */
    public Optional<PsiClass> getCurClass() {
        return getCurJavaFile().map(PsiUtils::getClassByFile);
    }

    /**
     * 获取当前打开的Java文件的顶层类
     *
     * @throws RuntimeException 如果找不到顶层类
     */
    public PsiClass getCurClassOrThrow() {
        return getCurJavaFile()
                .map(PsiUtils::getClassByFile)
                .orElseThrow(() -> new RuntimeException("getCurClass fail."));
    }

    /**
     * 获取当前选中的类
     */
    public Optional<PsiClass> getSelectedClass() {
        return cache("getSelectedClass", () ->
                Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT))
                        .filter(it -> it instanceof PsiClass)
                        .map(it -> (PsiClass) it));
    }

    /**
     * 获取当前选中的方法
     */
    public Optional<PsiMethod> getSelectedMethod() {
        return cache("getSelectedMethod", () ->
                Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT))
                        .filter(it -> it instanceof PsiMethod)
                        .map(it -> (PsiMethod) it));
    }

    /**
     * 根据文件名查找文件，只获取第一个文件
     */
    public Optional<VirtualFile> getVirtualFilesByName(String name) {
        return IndexUtils.getVirtualFilesByName(getProject(), name)
                .stream().findFirst();
    }


    /**
     * 根据文件名查找文件，只获取第一个
     */
    public Optional<PsiFile> getFilesByName(String name) {
        PsiFile[] files = IndexUtils.getFilesByName(getProject(), name);
        if (files != null && files.length > 0) {
            return Optional.ofNullable(files[0]);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T> T cache(String key, Supplier<T> valueSupplier) {
        return (T) cacheMap.computeIfAbsent(key, it -> valueSupplier.get());
    }
}
