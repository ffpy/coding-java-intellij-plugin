package org.ffpy.plugin.coding.action.env;

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

import java.util.Optional;

@RequiredArgsConstructor
public class ActionEnvImpl implements ActionEnv {

    /** 当前Event */
    @Getter
    @NonNull
    private final AnActionEvent event;

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
        return JavaPsiFacade.getElementFactory(getProject());
    }

    @Cache
    @Override
    public WriteActions getWriteActions() {
        return new WriteActions(getProject());
    }

    @Cache
    @Override
    public PsiDirectoryFactory getDirectoryFactory() {
        return PsiDirectoryFactory.getInstance(getProject());
    }

    @Cache
    @Override
    public PsiFileFactory getFileFactory() {
        return PsiFileFactory.getInstance(getProject());
    }

    @Cache
    @Override
    public VirtualFile getProjectRootFile() {
        return Optional.ofNullable(ProjectUtils.getRootFile(getProject()))
                .orElseThrow(() -> new RuntimeException("getProjectRootFile fail."));
    }

    @Override
    public SettingService getSettingService() {
        return ServiceManager.getService(getProject(), SettingService.class);
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
        return PsiManager.getInstance(getProject());
    }

    @Cache
    @Override
    public Optional<PsiClass> getCurClass() {
        return getCurJavaFile().map(PsiUtils::getClassByFile);
    }

    @Override
    public PsiClass getCurClassOrThrow() {
        return getCurJavaFile()
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
}
