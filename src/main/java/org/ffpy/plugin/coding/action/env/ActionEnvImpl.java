package org.ffpy.plugin.coding.action.env;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.ffpy.plugin.coding.constant.TemplateName;
import org.ffpy.plugin.coding.service.SettingService;
import org.ffpy.plugin.coding.util.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class ActionEnvImpl implements ActionEnv {

    /** 当前Event */
    @Getter
    @NonNull
    private final AnActionEvent event;

    @Setter
    private ActionEnv self;

    @Cache
    @Override
    public Optional<PsiFile> getCurFile() {
        return Optional.ofNullable(event.getData(LangDataKeys.PSI_FILE));
    }

    @Cache
    @Override
    public Optional<PsiJavaFile> getCurJavaFile() {
        return getCurFile().map(it -> (PsiJavaFile) it);
    }

    @Cache
    @Override
    public Project getProject() {
        return Optional.ofNullable(event.getProject())
                .orElseThrow(() -> new RuntimeException("getProject fail."));
    }

    @Cache
    @Override
    public PsiElementFactory getElementFactory() {
        return JavaPsiFacade.getElementFactory(self.getProject());
    }

    @Cache
    @Override
    public WriteActions getWriteActions() {
        return new WriteActions(self.getProject());
    }

    @Cache
    @Override
    public PsiDirectoryFactory getDirectoryFactory() {
        return PsiDirectoryFactory.getInstance(self.getProject());
    }

    @Cache
    @Override
    public PsiFileFactory getFileFactory() {
        return PsiFileFactory.getInstance(self.getProject());
    }

    @Cache
    @Override
    public VirtualFile getProjectRootFile() {
        return Optional.ofNullable(ProjectUtils.getRootFile(self.getProject()))
                .orElseThrow(() -> new RuntimeException("getProjectRootFile fail."));
    }

    @Override
    public SettingService getSettingService() {
        return ServiceManager.getService(self.getProject(), SettingService.class);
    }

    @Cache
    @Override
    public Optional<Editor> getEditor() {
        return Optional.ofNullable(event.getData(LangDataKeys.HOST_EDITOR));
    }

    @Cache
    @Override
    public Optional<PsiElement> getCurElement() {
        return Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT));
    }

    @Cache
    @Override
    public PsiManager getPsiManager() {
        return PsiManager.getInstance(self.getProject());
    }

    @Cache
    @Override
    public Optional<PsiClass> getCurClass() {
        return self.getCurJavaFile().map(PsiUtils::getClassByFile);
    }

    @Override
    public PsiClass getCurClassOrThrow() {
        return self.getCurJavaFile()
                .map(PsiUtils::getClassByFile)
                .orElseThrow(() -> new RuntimeException("getCurClass fail."));
    }

    @Cache
    @Override
    public Optional<PsiClass> getSelectedClass() {
        return Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT))
                .filter(it -> it instanceof PsiClass)
                .map(it -> (PsiClass) it);
    }

    @Cache
    @Override
    public Optional<PsiMethod> getSelectedMethod() {
        return Optional.ofNullable(event.getData(LangDataKeys.PSI_ELEMENT))
                .filter(it -> it instanceof PsiMethod)
                .map(it -> (PsiMethod) it);
    }

    @Override
    public Optional<VirtualFile> getVirtualFilesByName(String name) {
        return IndexUtils.getVirtualFilesByName(getProject(), name)
                .stream().findFirst();
    }

    @Override
    public Optional<PsiFile> getFilesByName(String name) {
        PsiFile[] files = IndexUtils.getFilesByName(getProject(), name);
        if (files != null && files.length > 0) {
            return Optional.ofNullable(files[0]);
        }
        return Optional.empty();
    }

    @Override
    public PsiFile createJavaFile(TemplateName templateName, String filename, Map<String, Object> params) {
        String content = TemplateUtils.fromString(self.getSettingService().getTemplate(templateName), params)
                .replace("\r\n", "\n");
        PsiFile file = self.getFileFactory().createFileFromText(JavaLanguage.INSTANCE, content);
        file.setName(StringHelper.toString(filename + ".java", params));
        return file;
    }

    @Override
    public TranslateHelper getTranslateHelper() {
        return new TranslateHelper(self.getProject());
    }

    @Override
    public PsiDirectory findOrCreateDirectoryByPackageName(String packageName) throws IOException {
        VirtualFile file = self.getProjectRootFile().findFileByRelativePath("src/main/java/");
        if (file == null) return null;
        String s = packageName;

        while (!s.isEmpty()) {
            String[] split = StringUtils.split(s, ".", 2);
            String dirName = split[0];
            s = split.length > 1 ? split[1] : "";

            file = findOrCreateDirectory(file, dirName);
        }
        return getDirectoryFactory().createDirectory(file);
    }

    private VirtualFile findOrCreateDirectory(VirtualFile file, String name) throws IOException {
        VirtualFile child = file.findChild(name);
        if (child == null) {
            child = file.createChildDirectory(this, name);
        }
        return child;
    }
}
