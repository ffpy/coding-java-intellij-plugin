package org.ffpy.plugin.coding.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class ProjectUtils {

    private static final String DEFAULT_PROJECT_NAME = "com.intellij.openapi.project.impl.DefaultProject";

    /**
     * 获取项目的根目录
     *
     * @param project project
     * @return 根目录
     */
    public static VirtualFile getRootFile(Project project) {
        return Optional.ofNullable(project)
                .map(Project::getProjectFile)
                .map(VirtualFile::getParent)
                .map(VirtualFile::getParent)
                .orElseGet(() -> {
                    log.error("get root dir fail!");
                    return null;
                });
    }

    /**
     * 判断是不是打包插件时的Project
     *
     * @param project project
     * @return true为是，false为否
     */
    public static boolean isDefaultProject(Project project) {
        return project != null && project.getClass().getName().startsWith(DEFAULT_PROJECT_NAME);
    }
}
