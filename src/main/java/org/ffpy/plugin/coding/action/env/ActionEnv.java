package org.ffpy.plugin.coding.action.env;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.ffpy.plugin.coding.constant.TemplateName;
import org.ffpy.plugin.coding.service.SettingService;
import org.ffpy.plugin.coding.util.TranslateHelper;
import org.ffpy.plugin.coding.util.WriteActions;

import java.util.Map;
import java.util.Optional;

public interface ActionEnv {

    /**
     * 获取当前Event
     */
    AnActionEvent getEvent();

    /**
     * 获取当前打开的文件
     */
    Optional<PsiFile> getCurFile();

    /**
     * 获取当前打开的Java文件
     */
    Optional<PsiJavaFile> getCurJavaFile();

    /**
     * 获取当前工程
     */
    Project getProject();

    /**
     * 获取PSI元素工厂
     */
    PsiElementFactory getElementFactory();

    /**
     * 获取WriteActions
     */
    WriteActions getWriteActions();

    /**
     * 获取目录工厂
     */
    PsiDirectoryFactory getDirectoryFactory();

    /**
     * 获取文件工程
     */
    PsiFileFactory getFileFactory();

    /**
     * 获取当前项目的根目录
     */
    VirtualFile getProjectRootFile();

    /**
     * 获取设置服务
     */
    SettingService getSettingService();

    /**
     * 获取编辑器实例
     */
    Optional<Editor> getEditor();

    /**
     * 获取当前光标所指元素
     */
    Optional<PsiElement> getCurElement();

    /**
     * 获取PSI管理器
     */
    PsiManager getPsiManager();

    /**
     * 获取当前打开的Java文件的顶层类
     */
    Optional<PsiClass> getCurClass();

    /**
     * 获取当前打开的Java文件的顶层类
     *
     * @throws RuntimeException 如果找不到顶层类
     */
    PsiClass getCurClassOrThrow();

    /**
     * 获取当前选中的类
     */
    Optional<PsiClass> getSelectedClass();

    /**
     * 获取当前选中的方法
     */
    Optional<PsiMethod> getSelectedMethod();

    /**
     * 根据文件名查找文件，只获取第一个文件
     *
     * @param name 文件名
     */
    Optional<VirtualFile> getVirtualFilesByName(String name);

    /**
     * 根据文件名查找文件，只获取第一个
     *
     * @param name 文件名
     */
    Optional<PsiFile> getFilesByName(String name);

    /**
     * 根据模板生成Java文件
     *
     * @param templateName 模板
     * @param filename     文件名，不带后缀
     * @param params       模板参数
     * @return 生成的Java文件
     */
    PsiFile createJavaFile(TemplateName templateName, String filename, Map<String, Object> params);

    /**
     * 获取翻译工具类
     */
    TranslateHelper getTranslateHelper();

    /**
     * 获取包目录/src/main/java/com.xxx
     */
    Optional<VirtualFile> getPackageFile();
}
