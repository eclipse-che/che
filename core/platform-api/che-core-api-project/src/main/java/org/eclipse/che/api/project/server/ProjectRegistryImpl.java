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
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches configuration
 * @author gazarenkov
 */
@Singleton
public class ProjectRegistryImpl implements ProjectRegistry {

    private final Map<String, RegisteredProject> projects;

    private final WorkspaceHolder        workspaceHolder;

    private final VirtualFileSystem   vfs;

    private final ProjectTypeRegistry projectTypeRegistry;

    private final ProjectHandlerRegistry handlers;

    private boolean initialized;

    @Inject
    public ProjectRegistryImpl(WorkspaceHolder workspaceHolder,
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
            RegisteredProject project = putProject(projectConfig, folder(projectConfig.getPath()), false, false);

            ProjectInitHandler handler = handlers.getProjectInitHandler(projectConfig.getType());
            if (handler != null) {
                handler.onProjectInitialized(folder(project.getPath()));
            }
        }

        // unconfigured folders on root
        FolderEntry root = new FolderEntry(vfs.getRoot());
        for (FolderEntry folder : root.getChildFolders()) {
            if (!projects.containsKey(folder.getVirtualFile().getPath().toString())) {
                putProject(null, folder, true, true);
            }
        }

        initialized = true;
    }

    @Override
    public String getWorkspaceId() {
        return workspaceHolder.getWorkspace().getId();
    }


    /**
     * @return all the projects
     * @throws ServerException
     *         if projects are not initialized yet
     */
    @Override
    public List<RegisteredProject> getProjects() throws ServerException {
        checkInitializationState();

        return new ArrayList<>(projects.values());
    }

    /**
     * @param projectPath
     * @return project or null if not found
     * @throws ServerException
     *         if projects are not initialized yet
     */
    @Override
    public RegisteredProject getProject(String projectPath) throws ServerException {
        checkInitializationState();

        return projects.get(absolutizePath(projectPath));
    }

    /**
     * @param parentPath
     *         where to find
     * @return child projects
     * @throws ServerException
     *         if projects are not initialized yet
     */
    @Override
    public List<String> getProjects(String parentPath) throws ServerException {
        checkInitializationState();

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
     * @throws ServerException
     *         if projects are not initialized yet
     */
    @Override
    public RegisteredProject getParentProject(String path) throws NotFoundException, ServerException {
        checkInitializationState();

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

    @Override
    public RegisteredProject putProject(ProjectConfig config, FolderEntry folder, boolean updated, boolean detected)
            throws ServerException, ConflictException,
                   NotFoundException, ForbiddenException {
        RegisteredProject project = new RegisteredProject(folder, config, updated, detected, this.projectTypeRegistry);
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
    @Override
    public RegisteredProject initProject(String projectPath, String type)
            throws ConflictException, ForbiddenException,
                   NotFoundException, ServerException {
        checkInitializationState();

        // it throws NFE if not here
        //RegisteredProject config = getParentProject(absolutizePath(ofPath));
//        FolderEntry baseFolder = folder(projectPath);

        int index = projectPath.lastIndexOf(File.separatorChar);
        String projectName = projectPath.substring(index + 1);
        NewProjectConfig conf = new NewProjectConfig(projectPath, projectName, type, null);

//        RegisteredProject project = new RegisteredProject(baseFolder, conf, true, this.projectTypeRegistry);
//        projects.put(project.getPath(), project);

        RegisteredProject project = putProject(conf, folder(projectPath), true, true);

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
    @Override
    public RegisteredProject reinitParentProject(String ofPath)
            throws ConflictException, ForbiddenException,
                   NotFoundException, ServerException {
        checkInitializationState();

        // it throws NFE if not here
        RegisteredProject config = getParentProject(absolutizePath(ofPath));

        RegisteredProject project = putProject(config, config.getBaseFolder(), true, config.isDetected());

        return project;
    }

    /**
     * removes all projects on and under the incoming path
     * @param path
     * @throws ServerException
     *         if projects are not initialized yet
     */
    @Override
    public void removeProjects(String path) throws ServerException {
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

    private void checkInitializationState() throws ServerException {
        if (!initialized) {
            throw new ServerException("Projects are not initialized yet");
        }
    }
}
