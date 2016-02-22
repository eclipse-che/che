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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.api.project.server.type.InvalidValueException;
import org.eclipse.che.api.project.server.type.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;

import javax.annotation.PostConstruct;
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

    private final VirtualFileSystem   vfs;

    private final ProjectTypeRegistry projectTypeRegistry;

    private final ProjectHandlerRegistry handlers;

    @Inject
    public ProjectRegistry(WorkspaceHolder  workspaceHolder,
                           VirtualFileSystemProvider vfsProvider,
                           ProjectTypeRegistry projectTypeRegistry,
                           ProjectHandlerRegistry handlers)
            throws ConflictException, NotFoundException,
                   ServerException, ForbiddenException {
        this.projects = new HashMap<>();
        this.workspaceHolder = workspaceHolder;
        this.vfs = vfsProvider.getVirtualFileSystem();
        this.projectTypeRegistry = projectTypeRegistry;
        this.handlers = handlers;
    }

    @PostConstruct
    void initProjects() throws ConflictException, NotFoundException, ServerException, ForbiddenException {
        UsersWorkspace workspace = workspaceHolder.getWorkspace();
        List<? extends ProjectConfig> projectConfigs = workspace.getConfig().getProjects();

        if (projectConfigs == null) {
            projectConfigs = new ArrayList<>();
        }

        for (ProjectConfig projectConfig : projectConfigs) {
            RegisteredProject project = putProject(projectConfig, folder(projectConfig.getPath()), false, true);

            ProjectInitHandler handler = handlers.getProjectInitHandler(projectConfig.getType());
            if (handler != null) {
                handler.onProjectInitialized(folder(project.getPath()));
            }
        }

        // unconfigured folders on root
        FolderEntry root = new FolderEntry(vfs.getRoot());
        for (FolderEntry folder : root.getChildFolders()) {
            if (!projects.containsKey(folder.getVirtualFile().getPath().toString())) {
                putProject(null, folder, true, false);
            }
        }
    }

    public String getWorkspaceId() {
        return workspaceHolder.getWorkspace().getId();
    }


    /**
     * @return all the projects
     */
    public List<RegisteredProject> getProjects() {
        return new ArrayList<>(projects.values());
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
//        if (projects.containsKey(path))
//            return projects.get(path);

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


    RegisteredProject putProject(ProjectConfig config, FolderEntry folder, boolean updated, boolean persisted)
            throws ServerException, ConflictException,
                   NotFoundException, ForbiddenException {
        RegisteredProject project = new RegisteredProject(folder, config, updated, persisted, this.projectTypeRegistry);
        projects.put(project.getPath(), project);
        return project;
    }

    /*  ------------------------------------------ */
    /*   to use from extension                     */
    /*  ------------------------------------------ */

    /**
     * To init new project from sources
     * @param projectPath
     * @param type
     * @return
     * @throws ProjectTypeConstraintException
     * @throws InvalidValueException
     * @throws ValueStorageException
     * @throws NotFoundException
     * @throws ServerException
     */
    public RegisteredProject initProject(String projectPath, String type)
            throws ConflictException, ForbiddenException,
                   NotFoundException, ServerException {

        // it throws NFE if not here
        //RegisteredProject config = getParentProject(absolutizePath(ofPath));
//        FolderEntry baseFolder = folder(projectPath);

        // new config
        //(path, type, mixins, name, description, attributes, source);
        NewProjectConfig conf = new NewProjectConfig(projectPath, type, null, null, null, null, null);

//        RegisteredProject project = new RegisteredProject(baseFolder, conf, true, this.projectTypeRegistry);
//        projects.put(project.getPath(), project);

        RegisteredProject project = putProject(conf, folder(projectPath), true, false);

        ProjectInitHandler handler = handlers.getProjectInitHandler(conf.getType());
        if(handler != null)
            handler.onProjectInitialized(folder(project.getPath()));


        return project;

    }

    /**
     * To reinit parent project
     * @param ofPath
     * @return
     * @throws ProjectTypeConstraintException
     * @throws InvalidValueException
     * @throws ValueStorageException
     * @throws NotFoundException
     * @throws ServerException
     */
    public RegisteredProject reinitParentProject(String ofPath)
            throws ConflictException, ForbiddenException,
                   NotFoundException, ServerException {

        // it throws NFE if not here
        RegisteredProject config = getParentProject(absolutizePath(ofPath));

        RegisteredProject project = putProject(config, config.getBaseFolder(), true, config.isDetected());

                //new RegisteredProject(config.getBaseFolder(), config, true, this.projectTypeRegistry);
        //projects.put(project.getPath(), project);

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

    static String absolutizePath(String path) {

        return (path.startsWith("/")) ? path : "/".concat(path);
    }

    FolderEntry folder(String path) throws ServerException {

        VirtualFile vf = vfs.getRoot().getChild(Path.of(path));
        return (vf == null) ? null : new FolderEntry(vf);
    }


}
