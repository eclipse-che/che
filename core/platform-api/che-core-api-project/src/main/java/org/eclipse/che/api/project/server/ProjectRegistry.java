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
import java.util.stream.Collectors;

/**
 * Stores internal representation of Projects registered in the Workspace Agent
 *
 * @author gazarenkov
 */
@Singleton
public class ProjectRegistry {

    private final Map<String, RegisteredProject> projects;
    private final WorkspaceHolder                workspaceHolder;
    private final VirtualFileSystem              vfs;
    private final ProjectTypeRegistry            projectTypeRegistry;
    private final ProjectHandlerRegistry         handlers;
    private final FolderEntry                    root;

    private boolean initialized;

    @Inject
    public ProjectRegistry(WorkspaceHolder workspaceHolder,
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

    /**
     * @return id of workspace this project belongs to
     */
    public String getWorkspaceId() {
        return workspaceHolder.getWorkspace().getId();
    }

    /**
     * @return all the registered projects
     */
    public List<RegisteredProject> getProjects()  {
        checkInitializationState();

        return new ArrayList<>(projects.values());
    }

    /**
     * @param projectPath - project path
     * @return project or null if not found
     */
    public RegisteredProject getProject(String projectPath) {
        checkInitializationState();

        return projects.get(absolutizePath(projectPath));
    }

    /**
     * @param parentPath - parent path
     * @return child projects
     */
    public List<String> getProjects(String parentPath)  {
        checkInitializationState();

        final Path root = Path.of(absolutizePath(parentPath));

        return projects.keySet().stream().filter(key -> Path.of(key).isChild(root))
                       .collect(Collectors.toList());
    }

    /**
     * @param path - path of child project
     * @return the project owned this path.
     */
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

    }

    /**
     * Creates RegisteredProject and caches it
     * @param config - project config
     * @param folder - base folder of project
     * @param updated - whether this configuration was updated
     * @param detected - whether this is automatically detected or explicitly defined project
     * @return project
     * @throws ServerException
     * @throws ConflictException
     * @throws NotFoundException
     * @throws ForbiddenException
     */
    RegisteredProject putProject(ProjectConfig config,
                                        FolderEntry folder,
                                        boolean updated,
                                        boolean detected) throws ServerException,
                                                                 ConflictException,
                                                                 NotFoundException,
                                                                 ForbiddenException {
        final RegisteredProject project = new RegisteredProject(folder, config, updated, detected, this.projectTypeRegistry);
        projects.put(project.getPath(), project);

//        if (initialized) {
//            workspaceHolder.updateProjects(projects.values());
//        }

        return project;
    }

    /**
     * removes all projects on and under the incoming path
     *
     * @param path - from where to remove
     */
     void removeProjects(String path) throws ServerException {
        projects.remove(path);
        getProjects(path).forEach(projects::remove);
    }

    /*  ------------------------------------------ */
    /*   to use from extension                     */
    /*  ------------------------------------------ */

    /**
     * Extension writer should call this method to apply changes which (supposedly) change
     * Attributes defined with particular Project Type
     * For example:
     * - extension code knows that particular file content is used by Value Provider
     * so this method should be called when content of this file changed to check
     * and update attributes.
     * OR
     * If Extension writer wants to force initializing folder to be Project
     * For example:
     * - extension code knows that particular folder inside should (or may) be treated
     * as sub-project of same as "parent" project type
     *
     * @param projectPath - absolute project path
     * @param type - type to be updated or added
     * @param asMixin - whether the type supposed to be mixin (true) or primary (false)
     * @return - refreshed project
     * @throws ConflictException
     * @throws ForbiddenException
     * @throws NotFoundException
     * @throws ServerException
     */
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

    /**
     * Extension writer should call this method to apply changes which supposedly
     * make the Project no longer have particular Project Type.
     * For example:
     * - extension code knows that removeing some file inside project's file system
     * will (or may) cause removing particular project type
     *
     * @param projectPath - project path
     * @param type - project type
     * @return - refreshed project
     * @throws ConflictException
     * @throws ForbiddenException
     * @throws NotFoundException
     * @throws ServerException
     */
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

    /**
     * @param path - a path
     * @return absolute (with lead slash) path
     */
    static String absolutizePath(String path) {
        return (path.startsWith("/")) ? path : "/".concat(path);
    }

    /**
     * Fires init handlers for all the project types of incoming project
     * @param project - the project
     * @throws ForbiddenException
     * @throws ConflictException
     * @throws NotFoundException
     * @throws ServerException
     */
    void fireInitHandlers(RegisteredProject project)
            throws ForbiddenException, ConflictException, NotFoundException, ServerException {

        // primary type
        //ProjectTypeDef pt = project.getProjectType();
        //pt.getAncestors();
        fireInit(project, project.getType());

        // mixins
        for(String mixin : project.getMixins()) {
            fireInit(project, mixin);
        }
    }

    void fireInit(RegisteredProject project, String type)
            throws ForbiddenException, ConflictException, NotFoundException, ServerException {
        ProjectInitHandler projectInitHandler = handlers.getProjectInitHandler(type);
        if (projectInitHandler != null) {
            projectInitHandler.onProjectInitialized(this, project.getBaseFolder());
        }
    }


    private void checkInitializationState() {
        if (!initialized) {
            throw new IllegalStateException("Projects are not initialized yet");
        }
    }

}
