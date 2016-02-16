/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.project.server.type.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystem;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches configuration
 * @author gazarenkov
 */
public class ProjectRegistry {

    private final Map<String, RegisteredProject> projects;

    private final WorkspaceHolder        workspaceHolder;

    private final LocalVirtualFileSystem vfs;

    private final ProjectTypeRegistry projectTypeRegistry;

    @Inject
    public ProjectRegistry(WorkspaceHolder  workspaceHolder, LocalVirtualFileSystem vfs,
                           ProjectTypeRegistry projectTypeRegistry)
            throws ProjectTypeConstraintException, InvalidValueException, NotFoundException,
                   ValueStorageException, ServerException {
        this.projects = new HashMap<>();
        this.workspaceHolder = workspaceHolder;
        this.vfs = vfs;
        this.projectTypeRegistry = projectTypeRegistry;

        initProjects();
    }

    public String getWorkspaceId() {
        return workspaceHolder.getWorkspace().getId();
    }


    /**
     * @return all the projects
     */
    public List<RegisteredProject> getProjects() {

        return new ArrayList(projects.values());
    }



    /**
     * @param projectPath
     * @return project or null if not found
     */
    public RegisteredProject getProject(String projectPath) {

        return projects.get(absolutizePath(projectPath));

    }

    /**
     * @param parentPath
     *         where to find
     * @return child projects
     */
    public List<String> getProjects(String parentPath) {

        Path root = Path.of(absolutizePath(parentPath));
        List<String> children = new ArrayList<>();

        for (String key : projects.keySet()) {
            if (Path.of(key).isChild(root)) {
                children.add(key);
            }
        }

        return children;
    }

    /**
     * @param path
     * @return the project owned this path.
     * @throws NotFoundException
     *         if not such a project found
     */
    public RegisteredProject getParentProject(String path) throws NotFoundException {

        // it is a project
        if (projects.containsKey(path))
            return projects.get(path);

        // otherwise try to find matched parent
        Path test;
        while ((test = Path.of(path).getParent()) != null) {
            RegisteredProject project = projects.get(test.toString());
            if (project != null)
                return project;

            path = test.toString();
        }

        // path is out of projects
        throw new NotFoundException("Parent project not found " + path);


    }


    public RegisteredProject putProject(ProjectConfig config, FolderEntry folder, boolean updated)
            throws ServerException, ProjectTypeConstraintException, InvalidValueException,
                   NotFoundException, ValueStorageException {

        RegisteredProject project = new RegisteredProject(folder, config, updated, this.projectTypeRegistry);
        projects.put(project.getPath(), project);

        return project;
    }

//    public RegisteredProject initProject(String projectPath, String type, List<String> mixins)
//            throws ProjectTypeConstraintException, InvalidValueException, ValueStorageException,
//                   NotFoundException, ServerException {
//
//        // it hrows NFE if not here
//        //RegisteredProject config = getParentProject(absolutizePath(ofPath));
//        FolderEntry baseFolder = folder(projectPath);
//
//        // imported config
//        ProjectManager.ImportedProjectConf conf = new ProjectManager.ImportedProjectConf(path, type, mixins, null /*source*/);
//
//        RegisteredProject project = new RegisteredProject(baseFolder, conf, true, this);
//        projects.put(project.getPath(), project);
//
//        return project;
//
//    }

    public RegisteredProject reinitProject(String ofPath)
            throws ProjectTypeConstraintException, InvalidValueException, ValueStorageException,
                   NotFoundException, ServerException {

        // it hrows NFE if not here
        RegisteredProject config = getParentProject(absolutizePath(ofPath));

        RegisteredProject project = new RegisteredProject(config.getBaseFolder(), config, true, this.projectTypeRegistry);
        projects.put(project.getPath(), project);

        return project;

    }



    /**
     * removes all projects on and under the incoming path
     * @param path
     */
    public void removeProjects(String path) {

        projects.remove(path);
        getProjects(path).forEach(projects::remove);
    }


     void initProjects()
            throws ProjectTypeConstraintException, InvalidValueException, ValueStorageException,
                   NotFoundException, ServerException {

        UsersWorkspace workspace = workspaceHolder.getWorkspace();
        List<? extends ProjectConfig> projectConfigs = workspace.getProjects();

        if(projectConfigs == null)
            projectConfigs = new ArrayList<>();

        for (ProjectConfig projectConfig : projectConfigs) {

            putProject(projectConfig, folder(projectConfig.getPath()), false);
            //initSubProjectsRecursively(projectConfig);

        }

         // unconfigured folders on root
         FolderEntry root = new FolderEntry(vfs.getRoot());
         for(FolderEntry folder : root.getChildFolders()) {
             if(!projects.containsKey(folder.getVirtualFile().getPath().toString())) {
                 putProject(null, folder, true);
             }
         }



    }


    static String absolutizePath(String path) {

        return (path.startsWith("/")) ? path : "/".concat(path);
    }

    FolderEntry folder(String path) throws ServerException {

        VirtualFile vf = vfs.getRoot().getChild(Path.of(path));
        return (vf == null) ? null : new FolderEntry(vf);
    }


}
