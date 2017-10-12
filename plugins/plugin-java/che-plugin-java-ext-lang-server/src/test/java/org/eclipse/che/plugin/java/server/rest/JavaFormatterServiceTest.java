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
package org.eclipse.che.plugin.java.server.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.WorkspaceProjectsSyncer;
import org.eclipse.che.api.project.server.WorkspaceSyncCommunication;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;
import org.eclipse.che.api.vfs.watcher.FileWatcherManager;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.plugin.java.server.projecttype.JavaProjectType;
import org.eclipse.che.plugin.java.server.projecttype.JavaValueProviderFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.corext.format.Formatter;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Test for the java formatter service. */
@Listeners(value = {MockitoTestNGListener.class})
public class JavaFormatterServiceTest {

  private static final String FORMATTER_CONTENT =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
          + "<profiles version=\"13\">\n"
          + "<profile kind=\"CodeFormatterProfile\" name=\"Eclipse2 [built-in]\" version=\"13\">\n"
          + "<setting id=\"org.eclipse.jdt.core.formatter.blank_lines_before_package\" value=\"3\"/>\n"
          + "</profile>\n"
          + "</profiles>";
  private static final String UPDATED_FORMATTER_CONTENT =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
          + "<profiles version=\"13\">\n"
          + "<profile kind=\"CodeFormatterProfile\" name=\"Eclipse2 [built-in]\" version=\"13\">\n"
          + "<setting id=\"org.eclipse.jdt.core.formatter.blank_lines_before_package\" value=\"5\"/>\n"
          + "</profile>\n"
          + "</profiles>";

  private JavaFormatterService service;
  private ProjectManager projectManager;

  @BeforeClass
  protected void initProjectApi() throws Exception {
    TestWorkspaceHolder workspaceHolder = new TestWorkspaceHolder(new ArrayList<>());
    File root = new File("target/test-classes");
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
    projectManager =
        new ProjectManager(
            vfsProvider,
            projectTypeRegistry,
            mock(WorkspaceSyncCommunication.class),
            projectRegistry,
            projectHandlerRegistry,
            importerRegistry,
            fileWatcherNotificationHandler,
            workspaceHolder,
            mock(FileWatcherManager.class));

    ResourcesPlugin resourcesPlugin =
        new ResourcesPlugin(
            "target/index", root.getAbsolutePath(), () -> projectRegistry, () -> projectManager);
    resourcesPlugin.start();

    JavaPlugin javaPlugin =
        new JavaPlugin(root.getAbsolutePath() + "/.settings", resourcesPlugin, projectRegistry);
    javaPlugin.start();

    JavaModelManager.getDeltaState().initializeRoots(true);

    createJavaFile();

    Formatter formatter = new Formatter();
    service = new JavaFormatterService(projectManager, formatter);
  }

  @Test
  public void cheFormatterShouldBeCreatedInRootFolder() throws Exception {
    service.updateRootFormatter(FORMATTER_CONTENT);

    checkRootFormatterFile();
  }

  @Test
  public void cheFormatterShouldBeUpdatedInRootFolder() throws Exception {
    service.updateRootFormatter(FORMATTER_CONTENT);

    checkRootFormatterFile();

    service.updateRootFormatter(UPDATED_FORMATTER_CONTENT);

    File updatedFormatterFile = new File("target/test-classes/.che/che-formatter.xml");
    assertTrue(updatedFormatterFile.exists());
    String updatedContent = Files.toString(updatedFormatterFile, Charsets.UTF_8);
    assertEquals(UPDATED_FORMATTER_CONTENT, updatedContent);
  }

  @Test
  public void cheFormatterShouldBeCreatedInProjectFolder() throws Exception {
    service.updateProjectFormatter("/FormatterTest", FORMATTER_CONTENT);

    checkProjectFormatterFile();
  }

  @Test
  public void cheFormatterShouldBeUpdatedInProjectFolder() throws Exception {
    service.updateProjectFormatter("/FormatterTest", FORMATTER_CONTENT);

    checkProjectFormatterFile();

    service.updateProjectFormatter("/FormatterTest", UPDATED_FORMATTER_CONTENT);

    File updatedFormatterFile =
        new File("target/test-classes/FormatterTest/.che/che-formatter.xml");
    assertTrue(updatedFormatterFile.exists());
    String updatedContent = Files.toString(updatedFormatterFile, Charsets.UTF_8);
    assertEquals(UPDATED_FORMATTER_CONTENT, updatedContent);
  }

  @Test(priority = 2)
  public void fileShouldBeFormattedViaProjectFormatter() throws Exception {
    File javaFile = new File("target/test-classes/FormatterTest/p1/X.java");
    assertTrue(javaFile.exists());
    String javaContent = Files.toString(javaFile, Charsets.UTF_8);

    //Formatter adds 3 empty line before package declaration
    service.updateProjectFormatter("/FormatterTest", FORMATTER_CONTENT);

    List<Change> formatChanges = service.getFormatChanges("/FormatterTest", 0, 56, javaContent);
    assertEquals("\n\n\n", formatChanges.get(2).getText());
  }

  @Test(priority = 1)
  public void fileShouldBeFormattedViaRootFormatter() throws Exception {
    File javaFile = new File("target/test-classes/FormatterTest/p1/X.java");
    assertTrue(javaFile.exists());
    String javaContent = Files.toString(javaFile, Charsets.UTF_8);

    //Formatter adds 5 empty line before package declaration
    service.updateRootFormatter(UPDATED_FORMATTER_CONTENT);

    List<Change> formatChanges = service.getFormatChanges("/FormatterTest", 0, 56, javaContent);
    assertEquals("\n\n\n\n\n", formatChanges.get(2).getText());
  }

  private void checkRootFormatterFile() throws IOException {
    File cheFolder = new File("target/test-classes/.che");
    assertTrue(cheFolder.exists());

    File formatterFile = new File("target/test-classes/.che/che-formatter.xml");
    assertTrue(formatterFile.exists());

    String content = Files.toString(formatterFile, Charsets.UTF_8);
    assertEquals(FORMATTER_CONTENT, content);
  }

  private void createJavaFile()
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    RegisteredProject project = projectManager.getProject("/FormatterTest");
    String classContent =
        "package p1;\n" + "public class X {\n" + "  public void foo() {\n" + "  }\n" + "}";
    FolderEntry baseFolder = project.getBaseFolder();
    FolderEntry packageFolder = baseFolder.createFolder("p1");
    packageFolder.createFile("X.java", classContent.getBytes());
  }

  private void checkProjectFormatterFile() throws IOException {
    File cheFolder = new File("target/test-classes/FormatterTest/.che");
    assertTrue(cheFolder.exists());

    File formatterFile = new File("target/test-classes/FormatterTest/.che/che-formatter.xml");
    assertTrue(formatterFile.exists());

    String content = Files.toString(formatterFile, Charsets.UTF_8);
    assertEquals(FORMATTER_CONTENT, content);
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
    protected void addProject(ProjectConfig project) throws ServerException {}

    @Override
    protected void updateProject(ProjectConfig project) throws ServerException {}

    @Override
    protected void removeProject(ProjectConfig project) throws ServerException {}
  }
}
