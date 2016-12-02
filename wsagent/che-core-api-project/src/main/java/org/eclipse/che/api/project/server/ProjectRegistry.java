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
import org.eclipse.che.api.core.model.project.NewProjectConfig;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Stores internal representation of Projects registered in the Workspace Agent.
 *
 * @author gazarenkov
 */
@Singleton
public class ProjectRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectRegistry.class);

    private final Map<String, RegisteredProject> projects;
    private final WorkspaceProjectsSyncer        workspaceHolder;
    private final VirtualFileSystem              vfs;
    private final ProjectTypeRegistry            projectTypeRegistry;
    private final ProjectHandlerRegistry         handlers;
    private final FolderEntry                    root;
    private final EventService                   eventService;

    private boolean initialized;

    @Inject
    public ProjectRegistry(WorkspaceProjectsSyncer workspaceHolder,
                           VirtualFileSystemProvider vfsProvider,
                           ProjectTypeRegistry projectTypeRegistry,
                           ProjectHandlerRegistry handlers,
                           EventService eventService) throws ServerException {
        this.eventService = eventService;
        this.projects = new ConcurrentHashMap<>();
        this.workspaceHolder = workspaceHolder;
        this.vfs = vfsProvider.getVirtualFileSystem();
        this.projectTypeRegistry = projectTypeRegistry;
        this.handlers = handlers;
        this.root = new FolderEntry(vfs.getRoot());
    }

    @PostConstruct
    public void initProjects() throws ConflictException, NotFoundException, ServerException, ForbiddenException {

        List<? extends ProjectConfig> projectConfigs = workspaceHolder.getProjects();

        // take all the projects from ws's config
        for (ProjectConfig projectConfig : projectConfigs) {
            final String path = projectConfig.getPath();
            final VirtualFile vf = vfs.getRoot().getChild(Path.of(path));
            final FolderEntry projectFolder = ((vf == null) ? null : new FolderEntry(vf, this));

            putProject(projectConfig, projectFolder, false, false);

        }

        initUnconfiguredFolders();

        initialized = true;

        for (RegisteredProject project : projects.values()) {
            // only for projects with sources
            if(project.getBaseFolder() != null) {
                fireInitHandlers(project);
            }
        }
    }


    /**
     * @return all the registered projects
     */
    public List<RegisteredProject> getProjects() {
        checkInitializationState();

        initUnconfiguredFolders();

        return new ArrayList<>(projects.values());
    }

    /**
     * @param projectPath
     *         project path
     * @return project or null if not found
     */
    public RegisteredProject getProject(String projectPath) {
        checkInitializationState();

        initUnconfiguredFolders();

        return projects.get(absolutizePath(projectPath));
    }

    /**
     * @param parentPath
     *         parent path
     * @return list projects of pojects
     */
    public List<String> getProjects(String parentPath) {
        checkInitializationState();

        initUnconfiguredFolders();

        final Path root = Path.of(absolutizePath(parentPath));

        return projects.keySet()
                       .stream()
                       .filter(key -> Path.of(key).isChild(root))
                       .collect(Collectors.toList());
    }

    /**
     * @param path
     *         - path of child project
     * @return the project owned this path.
     */
    public RegisteredProject getParentProject(String path) {
        checkInitializationState();

        // return this if a project
        if (getProject(path) != null) {
            return getProject(path);
        }

        // otherwise try to find matched parent
        Path test;
        while ((test = Path.of(path).getParent()) != null) {
            final RegisteredProject project = projects.get(test.toString());
            if (project != null) {
                return project;
            }

            path = test.toString();
        }

        return null;
    }

    /**
     * Creates RegisteredProject and caches it.
     *
     * @param config
     *         project config
     * @param folder
     *         base folder of project
     * @param updated
     *         whether this configuration was updated
     * @param detected
     *         whether this is automatically detected or explicitly defined project
     * @return project
     * @throws ServerException
     *         when path for project is undefined
     */
    RegisteredProject putProject(ProjectConfig config,
                                 FolderEntry folder,
                                 boolean updated,
                                 boolean detected) throws ServerException {

        final RegisteredProject project = new RegisteredProject(folder, config, updated, detected, this.projectTypeRegistry);
        projects.put(project.getPath(), project);

        return project;
    }

    /**
     * Removes all projects on and under the incoming path.
     *
     * @param path
     *         from where to remove
     * @throws ServerException
     */
    void removeProjects(String path) throws ServerException {

        List<RegisteredProject> removed = new ArrayList<>();
        Optional.ofNullable(projects.remove(path)).ifPresent(removed::add);
        getProjects(path).forEach(p -> Optional.ofNullable(projects.remove(p))
                                               .ifPresent(removed::add));

        removed.forEach(registeredProject -> eventService.publish(new ProjectDeletedEvent(registeredProject.getPath())));
    }

    /*  ------------------------------------------ */
    /*   to use from extension                     */
    /*  ------------------------------------------ */

    /**
     * Extension writer should call this method to apply changes which (supposedly) change
     * Attributes defined with particular Project Type
     * If incoming Project Type is primary and:
     * - If the folder located on projectPath is a Project, its Primary PT will be converted to incoming PT
     * - If the folder located on projectPath is NOT a Project the folder will be converted to "detected" Project with incoming Primary PT
     * If incoming Project Type is mixin and:
     * - If the folder located on projectPath is a Project, this PT will be added (if not already there) to its Mixin PTs
     * - If the folder located on projectPath is NOT a Project - ConflictException will be thrown
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
     * @param projectPath
     *         absolute project path
     * @param type
     *         type to be updated or added
     * @param asMixin
     *         whether the type supposed to be mixin (true) or primary (false)
     * @return refreshed project
     * @throws ConflictException
     * @throws NotFoundException
     * @throws ServerException
     */
    public RegisteredProject setProjectType(String projectPath,
                                            String type,
                                            boolean asMixin) throws ConflictException,
                                                                    NotFoundException,
                                                                    ServerException {
        final RegisteredProject project = getProject(projectPath);
        final NewProjectConfig conf;
        List<String> newMixins = new ArrayList<>();


        if (project == null) {
            if (asMixin) {
                throw new ConflictException("Can not assign as mixin type '" + type +
                                            "' since the " + projectPath + " is not a project.");
            } else {

                final String path = absolutizePath(projectPath);
                final String name = Path.of(projectPath).getName();

                conf = new NewProjectConfigImpl(path, type, newMixins, name, name, null, null, null);

                return putProject(conf, root.getChildFolder(path), true, true);
            }
        } else {
            newMixins = project.getMixins();
            String newType = project.getType();
            if (asMixin) {
                if (!newMixins.contains(type)) {
                    newMixins.add(type);
                }
            } else {
                newType = type;
            }

            conf = new NewProjectConfigImpl(project.getPath(),
                                        newType,
                                        newMixins,
                                        project.getName(),
                                        project.getDescription(),
                                        project.getAttributes(),
                                        null,
                                        project.getSource());

            return putProject(conf, project.getBaseFolder(), true, project.isDetected());
        }
    }

    /**
     * Extension writer should call this method to apply changes which supposedly
     * make the Project no longer have particular Project Type.
     * In a case of removing primary project type:
     * - if the project was NOT detected BASE Project Type will be set as primary
     * - if the project was detected it will be converted back to the folder
     * For example:
     * - extension code knows that removing some file inside project's file system
     * will (or may) cause removing particular project type
     *
     * @param projectPath
     *         project path
     * @param type
     *         project type
     * @return refreshed project or null if such a project not found or was removed
     * @throws ConflictException
     * @throws ForbiddenException
     * @throws NotFoundException
     * @throws ServerException
     */
    public RegisteredProject removeProjectType(String projectPath, String type) throws ConflictException,
                                                                                       ForbiddenException,
                                                                                       NotFoundException,
                                                                                       ServerException {
        final RegisteredProject project = getProject(projectPath);

        if (project == null) {
            return null;
        }

        List<String> newMixins = project.getMixins();
        String newType = project.getType();

        if (newMixins.contains(type)) {
            newMixins.remove(type);
        } else if (newType.equals(type)) {
            if (project.isDetected()) {
                projects.remove(project.getPath());
                return null;
            }

            newType = BaseProjectType.ID;
        }

        final NewProjectConfig conf = new NewProjectConfigImpl(project.getPath(),
                                                           newType,
                                                           newMixins,
                                                           project.getName(),
                                                           project.getDescription(),
                                                           project.getAttributes(),
                                                           null,
                                                           project.getSource());

        return putProject(conf, project.getBaseFolder(), true, project.isDetected());
    }

    /**
     * @param path
     *         a path
     * @return absolute (with lead slash) path
     */
    static String absolutizePath(String path) {
        return (path.startsWith("/")) ? path : "/".concat(path);
    }

    /** Try to initialize projects from unconfigured folders on root. */
    private void initUnconfiguredFolders() {
        try {
            for (FolderEntry folder : root.getChildFolders()) {
                if (!projects.containsKey(folder.getVirtualFile().getPath().toString())) {
                    putProject(null, folder, true, false);
                }
            }
        } catch (ServerException e) {
            LOG.warn(e.getLocalizedMessage());
        }
    }

    /**
     * Fires init handlers for all the project types of incoming project.
     *
     * @param project
     *         the project
     * @throws ForbiddenException
     * @throws ConflictException
     * @throws NotFoundException
     * @throws ServerException
     */
    void fireInitHandlers(RegisteredProject project) throws ForbiddenException,
                                                            ConflictException,
                                                            NotFoundException,
                                                            ServerException {
        // primary type
        fireInit(project, project.getType());

        // mixins
        for (String mixin : project.getMixins()) {
            fireInit(project, mixin);
        }
    }

    void fireInit(RegisteredProject project, String type) throws ForbiddenException,
                                                                 ConflictException,
                                                                 NotFoundException,
                                                                 ServerException {
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
