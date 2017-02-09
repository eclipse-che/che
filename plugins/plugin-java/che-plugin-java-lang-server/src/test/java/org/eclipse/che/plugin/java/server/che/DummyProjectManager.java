/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.che;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Dummy implementation of ProjectManager used for tests
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
// TODO: rework after new Project API
public class DummyProjectManager /*implements ProjectManager*/ {


    final String      vfsUser       = "dev";
//    private final LocalFileSystemProvider localFileSystemProvider;

    public DummyProjectManager(String workspacePath, EventService eventService) {

        EnvironmentContext context = new EnvironmentContext();
        context.setSubject(new SubjectImpl(vfsUser, "", "", false));
        EnvironmentContext.setCurrent(context);
//        localFileSystemProvider = new LocalFileSystemProvider("", new LocalFSMountStrategy() {
//            @Override
//            public File getMountPath(String workspaceId) throws ServerException {
//                return new File(workspacePath);
//            }
//
//            @Override
//            public File getMountPath() throws ServerException {
//                return new File(workspacePath);
//            }
//        }, eventService, null, SystemPathsFilter.ANY, null);
    }
//
//    @Override
//    public List<Project> getProjects(String workspace) throws ServerException, NotFoundException, ForbiddenException {
//        final FolderEntry myRoot = getProjectsRoot(workspace);
//        final List<Project> projects = new ArrayList<>();
//        for (FolderEntry folder : myRoot.getChildFolders()) {
//            final Project project = getProject(workspace, folder.getPath());
//            if (project != null) {
//                projects.add(project);
//            }
//        }
//        return projects;
//    }
//
//    @Override
//    public Project getProject(String workspace, String projectPath) throws ForbiddenException, ServerException, NotFoundException {
//        final FolderEntry myRoot = getProjectsRoot(workspace);
//        final VirtualFileEntry child = myRoot.getChild(projectPath.startsWith("/") ? projectPath.substring(1) : projectPath);
//        if (child != null && child.isFolder() && isProjectFolder((FolderEntry)child)) {
//            return new Project((FolderEntry)child, this);
//        }
//        return null;
//    }
//
//    @Override
//    public ProjectConfigDto getProjectFromWorkspace(@NotNull String wsId, @NotNull String projectPath) throws ServerException {
//        throw new UnsupportedOperationException("The method unsupported in this mode.");
//    }
//
//    @Override
//    public List<ProjectConfigDto> getAllProjectsFromWorkspace(@NotNull String workspaceId) throws ServerException {
//        throw new UnsupportedOperationException("The method unsupported in this mode.");
//    }
//
//    @Override
//    public Project createProject(String workspace, String name, ProjectConfig projectConfig, Map<String, String> options)
//            throws ConflictException, ForbiddenException, ServerException, ProjectTypeConstraintException, NotFoundException {
//        final FolderEntry myRoot = getProjectsRoot(workspace);
//        final FolderEntry projectFolder = myRoot.createFolder(name);
//        final Project project = new Project(projectFolder, this);
//        return project;
//    }
//
//    @Override
//    public Project updateProject(String workspace, String path, ProjectConfig newConfig)
//            throws ForbiddenException, ServerException, NotFoundException, ConflictException, IOException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public FolderEntry getProjectsRoot(String workspace) throws ServerException, NotFoundException {
//        return new FolderEntry(workspace, localFileSystemProvider.getMountPoint(true).getRoot());
//    }
//
//    @Override
//    public ProjectConfig getProjectConfig(Project project) throws ServerException, ProjectTypeConstraintException, ValueStorageException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void updateProjectConfig(Project project, ProjectConfig config)
//            throws ServerException, ValueStorageException, ProjectTypeConstraintException, InvalidValueException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public ProjectMisc getProjectMisc(Project project) throws ServerException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void saveProjectMisc(Project project, ProjectMisc misc) throws ServerException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public List<ProjectConfig> getProjectModules(Project project) throws ServerException,
//                                                                         ForbiddenException,
//                                                                         ConflictException,
//                                                                         IOException,
//                                                                         NotFoundException {
//        return Collections.emptyList();
//    }
//
//    @Override
//    public VirtualFileSystemRegistry getVirtualFileSystemRegistry() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public ProjectTypeRegistry getProjectTypeRegistry() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public ProjectHandlerRegistry getHandlers() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Map<String, AttributeValue> estimateProject(String workspace, String path, String projectTypeId)
//            throws ValueStorageException, ServerException, ForbiddenException, NotFoundException, ProjectTypeConstraintException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public ProjectConfigDto addModule(String workspace,
//                                      String modulePath,
//                                      ProjectConfigDto moduleConfig,
//                                      Map<String, String> options)
//            throws ConflictException, ForbiddenException, ServerException, NotFoundException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public List<SourceEstimation> resolveSources(String workspace, String path, boolean transientOnly)
//            throws ServerException, ForbiddenException, NotFoundException, ValueStorageException, ProjectTypeConstraintException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Project convertFolderToProject(String workspace, String path, ProjectConfig projectConfig)
//            throws ConflictException, ForbiddenException, ServerException, NotFoundException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public VirtualFileEntry rename(String workspace, String path, String newName, String newMediaType)
//            throws ForbiddenException, ServerException, ConflictException, NotFoundException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void delete(String workspace, String path) throws ServerException, ForbiddenException, NotFoundException, ConflictException {
//        deleteEntry(workspace, path);
//    }
//
//    private void deleteEntry(String workspace, String deleteEntryPath) throws ServerException, NotFoundException, ForbiddenException {
//        final FolderEntry root = getProjectsRoot(workspace);
//        final VirtualFileEntry entry = root.getChild(deleteEntryPath);
//
//        entry.remove();
//    }
//
//    @Override
//    public void deleteModule(String workspaceId, String pathToParent, String pathToModule) throws ServerException,
//                                                                                                  NotFoundException,
//                                                                                                  ForbiddenException,
//                                                                                                  ConflictException {
//        deleteEntry(workspaceId, pathToModule);
//    }
//
//    @Override
//    public boolean isProjectFolder(FolderEntry folder) throws ServerException {
//        return new Path(folder.getPath()).segmentCount() == 1;
//    }
//
//    @Override
//    public boolean isModuleFolder(FolderEntry folder) throws ServerException {
//        return false;
//    }
}
