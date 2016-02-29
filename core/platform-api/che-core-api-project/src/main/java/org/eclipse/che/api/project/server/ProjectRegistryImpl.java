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

        // take all the projects from ws's config
        for (ProjectConfig projectConfig : projectConfigs) {
             String path = projectConfig.getPath();
             VirtualFile vf = vfs.getRoot().getChild(Path.of(path));
             FolderEntry projFolder = ((vf == null) ? null : new FolderEntry(vf, this));
             putProject(projectConfig, projFolder, false, false);
        }

        // unconfigured folders on root
        for (FolderEntry folder : root.getChildFolders()) {
            if (!projects.containsKey(folder.getVirtualFile().getPath().toString())) {
                putProject(null, folder, true, false);
            }
        }

        for(RegisteredProject project : projects.values()) {
            fireInitHandlers(project);
        }

        initialized = true;
    }

    @Override
    public String getWorkspaceId() {
        return workspaceHolder.getWorkspace().getId();
    }

    @Override
    public List<RegisteredProject> getProjects()  {
        checkInitializationState();

        return new ArrayList<>(projects.values());
    }

    @Override
    public RegisteredProject getProject(String projectPath) {
        checkInitializationState();

        return projects.get(absolutizePath(projectPath));
    }

    @Override
    public List<String> getProjects(String parentPath)  {
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
    public RegisteredProject getParentProject(String path)  {
        checkInitializationState();

        // return this if a project
        if(getProject(path) != null)
            return getProject(path);

        // otherwise try to find matched parent
        Path test;
        while ((test = Path.of(path).getParent()) != null) {
            RegisteredProject project = projects.get(test.toString());
            if (project != null)
                return project;

            path = test.toString();
        }
        return null;

        // path is out of projects
        //throw new NotFoundException("Parent project not found " + path);
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

//    private FolderEntry folder(String path) throws ServerException {
//        VirtualFile vf = vfs.getRoot().getChild(Path.of(path));
//        return (vf == null) ? null : new FolderEntry(vf, path);
//    }

    void fireInitHandlers(RegisteredProject project)
            throws ForbiddenException, ConflictException, NotFoundException, ServerException {

        // primary type
        ProjectInitHandler projectInitHandler = handlers.getProjectInitHandler(project.getType());
        if (projectInitHandler != null) {
            projectInitHandler.onProjectInitialized(project.getBaseFolder());
        }

        // mixins
        for(String mixin : project.getMixins()) {
            projectInitHandler = handlers.getProjectInitHandler(mixin);
            if (projectInitHandler != null) {
                projectInitHandler.onProjectInitialized(project.getBaseFolder());
            }
        }
    }


    private void checkInitializationState() {
        if (!initialized) {
            throw new IllegalStateException("Projects are not initialized yet");
        }
    }

}
