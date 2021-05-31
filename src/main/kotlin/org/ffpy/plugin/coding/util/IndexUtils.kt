package org.ffpy.plugin.coding.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex
import com.intellij.psi.impl.java.stubs.index.JavaShortClassNameIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

object IndexUtils {

    fun getVirtualFilesByName(project: Project, name: String): Collection<VirtualFile> {
        return getVirtualFilesByName(project, name, GlobalSearchScope.projectScope(project))
    }

    fun getVirtualFilesByName(project: Project, name: String, scope: GlobalSearchScope): Collection<VirtualFile> {
        return ApplicationManager.getApplication().runReadAction(Computable {
            FilenameIndex.getVirtualFilesByName(project, name, scope)
        } as Computable<Collection<VirtualFile>>)
    }

    fun getFilesByName(project: Project, name: String): Array<PsiFile> {
        return getFilesByName(project, name, GlobalSearchScope.projectScope(project))
    }

    fun getFilesByName(project: Project, name: String, scope: GlobalSearchScope): Array<PsiFile> {
        return ApplicationManager.getApplication().runReadAction(Computable {
            FilenameIndex.getFilesByName(
                project, name, scope
            )
        } as Computable<Array<PsiFile>>)
    }

    fun getClassByShortName(project: Project, shortName: String): PsiClass {
        return getClassByShortName(project, shortName, GlobalSearchScope.projectScope(project))
    }

    fun getClassByShortName(project: Project, shortName: String, scope: GlobalSearchScope): PsiClass {
        return JavaShortClassNameIndex.getInstance()[shortName, project, scope].stream()
            .findFirst()
            .orElse(null)
    }

    fun getClassByQualifiedName(project: Project, qualifiedName: String): PsiClass {
        return getClassByQualifiedName(project, qualifiedName, GlobalSearchScope.projectScope(project))
    }

    fun getClassByQualifiedName(project: Project, qualifiedName: String, scope: GlobalSearchScope): PsiClass {
        return JavaFullClassNameIndex.getInstance()[qualifiedName.hashCode(), project, scope].stream()
            .filter { psiClass: PsiClass -> qualifiedName == psiClass.qualifiedName }
            .findFirst()
            .orElse(null)
    }
}