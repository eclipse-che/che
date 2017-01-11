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
package org.eclipse.che.plugin.java.plain.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.WorkspaceProjectsSyncer;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.watcher.FileWatcherManager;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.jdt.core.resources.ResourceChangedEvent;
import org.eclipse.che.plugin.java.plain.server.projecttype.PlainJavaProjectType;
import org.eclipse.che.plugin.java.plain.server.projecttype.PlainJavaValueProviderFactory;
import org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants;
import org.eclipse.che.plugin.java.server.projecttype.JavaProjectType;
import org.eclipse.che.plugin.java.server.projecttype.JavaValueProviderFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;

/**
 * @author Valeriy Svydenko
 */
public abstract class BaseTest {
    private final static String wsPath     = BaseTest.class.getResource("/projects").getFile();
    private final static String INDEX_PATH = "target/fs_index";

    protected File                           root;
    protected ProjectRegistry                projectRegistry;
    protected ProjectManager                 projectManager;
    protected LocalVirtualFileSystemProvider vfsProvider;

    @BeforeClass
    protected void initProjectApi() throws Exception {
        JavaPlugin javaPlugin = new JavaPlugin(wsPath + "/set", null, null);
        EventService eventService = new EventService();

        TestWorkspaceHolder workspaceHolder = new TestWorkspaceHolder();

        if (root == null)
            root = new File(wsPath);

        if (root.exists()) {
            IoUtil.deleteRecursive(root);
        }
        root.mkdir();

        File indexDir = new File(INDEX_PATH);

        if (indexDir.exists()) {
            IoUtil.deleteRecursive(indexDir);
        }
        indexDir.mkdir();

        Set<PathMatcher> filters = new HashSet<>();
        filters.add(path -> true);

        FSLuceneSearcherProvider sProvider = new FSLuceneSearcherProvider(indexDir, filters);
        vfsProvider = new LocalVirtualFileSystemProvider(root, sProvider);
        ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());

        projectTypeRegistry.registerProjectType(new JavaProjectType(new JavaValueProviderFactory()));
        projectTypeRegistry.registerProjectType(new PlainJavaProjectType(new PlainJavaValueProviderFactory()));

        ProjectHandlerRegistry projectHandlerRegistry = new ProjectHandlerRegistry(new HashSet<>());

        projectRegistry = new ProjectRegistry(workspaceHolder, vfsProvider, projectTypeRegistry, projectHandlerRegistry, eventService);
        projectRegistry.initProjects();

        ProjectImporterRegistry importerRegistry = new ProjectImporterRegistry(new HashSet<>());
        FileWatcherNotificationHandler fileWatcherNotificationHandler = new DefaultFileWatcherNotificationHandler(vfsProvider);
        FileTreeWatcher fileTreeWatcher = new FileTreeWatcher(root, new HashSet<>(), fileWatcherNotificationHandler);

        projectManager = new ProjectManager(vfsProvider,
                                            eventService,
                                            projectTypeRegistry,
                                            projectRegistry,
                                            projectHandlerRegistry,
                                            importerRegistry,
                                            fileWatcherNotificationHandler,
                                            fileTreeWatcher,
                                            new TestWorkspaceHolder(new ArrayList<>()),
                                            mock(FileWatcherManager.class));

        ResourcesPlugin plugin = new ResourcesPlugin("target/index", wsPath, () -> projectRegistry, () -> projectManager);

        plugin.start();
        javaPlugin.start();
    }

    @AfterMethod
    public void shutdownMavenServer() throws Exception {
        JavaModelManager.getJavaModelManager().deltaState.removeExternalElementsToRefresh();
    }

    protected FolderEntry createTestProject() throws ServerException, NotFoundException, ConflictException, ForbiddenException {
        String classpath = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<classpath>\n" +
                           "\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n" +
                           "</classpath>";

        FolderEntry parent = projectManager.getProjectsRoot().createFolder("project");
        parent.createFolder("bin");
        parent.createFolder("src");
        FolderEntry codenvyFolder = parent.createFolder(".che");
        FolderEntry libFolder = parent.createFolder("lib");

        libFolder.createFile("a.jar", "text".getBytes());
        codenvyFolder.createFile("classpath", classpath.getBytes());

        projectRegistry.setProjectType(parent.getPath().toString(), PlainJavaProjectConstants.JAVAC_PROJECT_ID, false);

        //inform DeltaProcessingStat about new project
        JavaModelManager.getJavaModelManager().deltaState.resourceChanged(
                new ResourceChangedEvent(root, new ProjectCreatedEvent("", parent.getPath().toString())));

        return parent;
    }

    private static class TestWorkspaceHolder extends WorkspaceProjectsSyncer {

        private List<ProjectConfigDto> projects;

        TestWorkspaceHolder() {
            this.projects = new ArrayList<>();
        }

        TestWorkspaceHolder(List<ProjectConfigDto> projects) {
            this.projects = projects;
        }

        @Override
        public List<? extends ProjectConfig> getProjects() {
            return projects;
        }

        @Override
        public String getWorkspaceId() {
            return "id";
        }

        @Override
        protected void addProject(ProjectConfig project) throws ServerException {

        }

        @Override
        protected void updateProject(ProjectConfig project) throws ServerException {

        }

        @Override
        protected void removeProject(ProjectConfig project) throws ServerException {

        }
    }
}
