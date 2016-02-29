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
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches configuration
 *
 * @author gazarenkov
 */
@Singleton
public class ProjectRegistryImpl implements ProjectRegistry {

    private final Map<String, RegisteredProject> projects;
    private final WorkspaceHolder                workspaceHolder;
    private final VirtualFileSystem              vfs;
    private final ProjectTypeRegistry            projectTypeRegistry;
    private final ProjectHandlerRegistry         handlers;
    private final FolderEntry                    root;

    private boolean initialized;

    @Inject
    public ProjectRegistryImpl(WorkspaceHolder workspaceHolder,
                               VirtualFileSystemProvider vfsProvider,
                               ProjectTypeRegistry projectTypeRegistry,
                               ProjectHandlerRegistry handlers) throws ServerException {
        this.projects = new HashMap<>();
        this.workspaceHolder = workspaceHolder;
        this.vfs = vfsProvider.getVirtualFileSystem();
        this.projectTypeRegistry = projectTypeRegistry;
        this.handlers = handlers;
        this.root = new FolderEntry(vfs.getRoot());
    }

    @PostConstruct
    public void initProjects() throws ConflictException, NotFoundException, ServerException, ForbiddenException {
        final UsersWorkspace workspace = workspaceHolder.getWorkspace();
        List<? extends ProjectConfig> projectConfigs = workspace.getConfig().getProjects();

        if (projectConfigs == null) {
            projectConfigs = new ArrayList<>();
        }

        for (ProjectConfig projectConfig : projectConfigs) {
            final RegisteredProject project = putProject(projectConfig, folder(projectConfig.getPath()), false, false);

            final ProjectInitHandler handler = handlers.getProjectInitHandler(projectConfig.getType());
            if (handler != null) {
                handler.onProjectInitialized(folder(project.getPath()));
            }
        }

        // unconfigured folders on root
        //final FolderEntry root = new FolderEntry(vfs.getRoot());
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

    @Override
    public List<RegisteredProject> getProjects() throws ServerException {
        checkInitializationState();

        return new ArrayList<>(projects.values());
    }

    @Override
    public RegisteredProject getProject(String projectPath) throws ServerException {
        checkInitializationState();

        return projects.get(absolutizePath(projectPath));
    }

    @Override
    public List<String> getProjects(String parentPath) throws ServerException {
        checkInitializationState();

        final Path root = Path.of(absolutizePath(parentPath));
        List<String> children = new ArrayList<>();

        for (String key : projects.keySet()) {
            if (Path.of(key).isChild(root)) {
                children.add(key);
            }
        }

        return children;
    }

    @Override
    public RegisteredProject getParentProject(String path) throws ServerException {
        checkInitializationState();

        // otherwise try to find matched parent
        Path test;
        while ((test = Path.of(path).getParent()) != null) {
            RegisteredProject project = projects.get(test.toString());
            if (project != null)
                return project;

            path = test.toString();
        }

        // path is out of projects
        return null;
    }

    @Override
    public RegisteredProject putProject(ProjectConfig config,
                                        FolderEntry folder,
                                        boolean updated,
                                        boolean detected) throws ServerException,
                                                                 ConflictException,
                                                                 NotFoundException,
                                                                 ForbiddenException {
        final RegisteredProject project = new RegisteredProject(folder, config, updated, detected, this.projectTypeRegistry);
        projects.put(project.getPath(), project);
        return project;
    }

    /*  ------------------------------------------ */
    /*   to use from extension                     */
    /*  ------------------------------------------ */


    public RegisteredProject setProjectType(String projectPath, String type,
                                            boolean asMixin) throws ConflictException,
                                                                    ForbiddenException,
                                                                    NotFoundException,
                                                                    ServerException {

        RegisteredProject project = getProject(projectPath);
        NewProjectConfig conf;
        List<String> newMixins = new ArrayList<>();
        String newType = null;

        if(project == null) {
            if(asMixin) {
                newMixins.add(type);
            } else {
                newType = type;
            }

            String path = absolutizePath(projectPath);
            String name = Path.of(projectPath).getName();
            conf = new NewProjectConfig(absolutizePath(projectPath), newType, newMixins,
                                        name, name, null, null);

            return putProject(conf, root.getChildFolder(path), true, true);

        } else {
            newMixins = project.getMixins();
            newType = project.getType();
            if(asMixin) {
                if(!newMixins.contains(type))
                    newMixins.add(type);
            } else {
                newType = type;
            }

            conf = new NewProjectConfig(project.getPath(), newType, newMixins,
                                        project.getName(), project.getDescription(),
                                        project.getAttributes(), project.getSource());
            return putProject(conf, project.getBaseFolder(), true, project.isDetected());

        }

    }

    public RegisteredProject removeProjectType(String projectPath, String type) throws ConflictException,
                                                                                       ForbiddenException,
                                                                                       NotFoundException,
                                                                                       ServerException {

        RegisteredProject project = getProject(projectPath);

        if(project == null)
            return null;

        List<String> newMixins = project.getMixins();
        String newType = project.getType();

        if(!newMixins.contains(type))
            newMixins.remove(type);
        else if(newType.equals(type))
            newType = BaseProjectType.ID;

        final NewProjectConfig conf = new NewProjectConfig(project.getPath(), newType, newMixins,
                                                           project.getName(), project.getDescription(),
                                                           project.getAttributes(), project.getSource());

        return putProject(conf, project.getBaseFolder(), true, project.isDetected());

    }


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
