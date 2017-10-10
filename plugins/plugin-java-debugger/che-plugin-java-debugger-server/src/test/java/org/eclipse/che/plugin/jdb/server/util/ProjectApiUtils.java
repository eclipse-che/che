/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.server.util;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.WorkspaceProjectsSyncer;
import org.eclipse.che.api.project.server.WorkspaceSyncCommunication;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;
import org.eclipse.che.api.vfs.watcher.FileWatcherManager;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.plugin.java.server.projecttype.JavaProjectType;
import org.eclipse.che.plugin.java.server.projecttype.JavaValueProviderFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;

/** @author Anatolii Bazko */
public class ProjectApiUtils {

  private static final AtomicBoolean initialized = new AtomicBoolean();

  /** Ensures that project api has been initialized only once. */
  public static void ensure() throws Exception {
    if (!initialized.get()) {
      synchronized (initialized) {
        if (!initialized.get()) {
          init();
          initialized.set(true);
        }
      }
    }
  }

  /** Initialize project API for tests. */
  private static void init() throws Exception {
    TestWorkspaceHolder workspaceHolder = new TestWorkspaceHolder(new ArrayList<>());
    File root = new File("target/test-classes/workspace");
    assertTrue(root.exists());

    File indexDir = new File("target/fs_index");
    assertTrue(indexDir.mkdirs());

    Set<PathMatcher> filters = new HashSet<>();
    filters.add(path -> true);
    FSLuceneSearcherProvider sProvider = new FSLuceneSearcherProvider(indexDir, filters);

    EventService eventService = new EventService();
    LocalVirtualFileSystemProvider vfsProvider =
        new LocalVirtualFileSystemProvider(root, sProvider);
    ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());
    projectTypeRegistry.registerProjectType(new JavaProjectType(new JavaValueProviderFactory()));
    ProjectHandlerRegistry projectHandlerRegistry = new ProjectHandlerRegistry(new HashSet<>());
    ProjectRegistry projectRegistry =
        new ProjectRegistry(
            workspaceHolder,
            vfsProvider,
            projectTypeRegistry,
            projectHandlerRegistry,
            eventService);
    projectRegistry.initProjects();

    ProjectImporterRegistry importerRegistry = new ProjectImporterRegistry(new HashSet<>());
    FileWatcherNotificationHandler fileWatcherNotificationHandler =
        new DefaultFileWatcherNotificationHandler(vfsProvider);
    FileTreeWatcher fileTreeWatcher =
        new FileTreeWatcher(root, new HashSet<>(), fileWatcherNotificationHandler);
    ProjectManager projectManager =
        new ProjectManager(
            vfsProvider,
            projectTypeRegistry,
            mock(WorkspaceSyncCommunication.class),
            projectRegistry,
            projectHandlerRegistry,
            importerRegistry,
            fileWatcherNotificationHandler,
            fileTreeWatcher,
            workspaceHolder,
            mock(FileWatcherManager.class));

    ResourcesPlugin resourcesPlugin =
        new ResourcesPlugin(
            "target/index", root.getAbsolutePath(), () -> projectRegistry, () -> projectManager);
    resourcesPlugin.start();

    JavaPlugin javaPlugin =
        new JavaPlugin(root.getAbsolutePath() + "/.settings", resourcesPlugin, projectRegistry);
    javaPlugin.start();

    projectRegistry.setProjectType("test", "java", false);

    JavaModelManager.getDeltaState().initializeRoots(true);
  }

  private static class TestWorkspaceHolder extends WorkspaceProjectsSyncer {
    private List<ProjectConfigDto> projects;

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
    protected void addProject(ProjectConfig project) throws ServerException {}

    @Override
    protected void updateProject(ProjectConfig project) throws ServerException {}

    @Override
    protected void removeProject(ProjectConfig project) throws ServerException {}
  }
}
