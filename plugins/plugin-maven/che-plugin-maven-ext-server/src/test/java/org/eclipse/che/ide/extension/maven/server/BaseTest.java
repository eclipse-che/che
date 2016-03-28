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
package org.eclipse.che.ide.extension.maven.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.TestWorkspaceHolder;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.jdt.core.resources.ResourceChangedEvent;
import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;

/**
 * @author Evgen Vidolob
 */
public abstract class BaseTest {

    protected static final String wsPath        = "target/workspace";
    protected final static String INDEX_PATH    = "target/fs_index";

    protected static Map<String, String> options = new HashMap<>();
    protected static EventService eventService = new EventService();
    protected static ResourcesPlugin plugin;
    protected static JavaPlugin        javaPlugin        = new JavaPlugin(wsPath + "/set");
    protected static FileBuffersPlugin fileBuffersPlugin = new FileBuffersPlugin();
    protected static TestWorkspaceHolder workspaceHolder;

    protected File root;

    protected ProjectManager pm;

    protected LocalVirtualFileSystemProvider vfsProvider;

    protected ProjectRegistry projectRegistry;

    protected FileWatcherNotificationHandler fileWatcherNotificationHandler;

    protected FileTreeWatcher fileTreeWatcher;

    protected ProjectTypeRegistry projectTypeRegistry;

    protected ProjectHandlerRegistry projectHandlerRegistry;

    protected ProjectImporterRegistry importerRegistry;

    private final String mavenServerPath = BaseTest.class.getResource("/maven-server").getPath();

    protected MavenServerManager mavenServerManager;
    public BaseTest() {
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.CORE_ENCODING, "UTF-8");
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        options.put(CompilerOptions.OPTION_TargetPlatform, JavaCore.VERSION_1_8);
        options.put(AssistOptions.OPTION_PerformVisibilityCheck, AssistOptions.ENABLED);
        options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_TaskTags, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.DISABLED);
        options.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        options.put(JavaCore.COMPILER_PB_UNUSED_PARAMETER_INCLUDE_DOC_COMMENT_REFERENCE, JavaCore.ENABLED);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        options.put(CompilerOptions.OPTION_Process_Annotations, JavaCore.DISABLED);
    }

    @BeforeMethod
    protected void initProjectApi() throws Exception {
        mavenServerManager = new MavenServerManager(mavenServerPath);
        workspaceHolder = new TestWorkspaceHolder();

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


        projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());
        projectTypeRegistry.registerProjectType(new TestProjectType());
        projectTypeRegistry.registerProjectType(new MavenProjectType());

        projectHandlerRegistry = new ProjectHandlerRegistry(new HashSet<>());

        projectRegistry = new ProjectRegistry(workspaceHolder, vfsProvider, projectTypeRegistry, projectHandlerRegistry);
        projectRegistry.initProjects();

        importerRegistry = new ProjectImporterRegistry(new HashSet<>());

        fileWatcherNotificationHandler = new DefaultFileWatcherNotificationHandler(vfsProvider);
        fileTreeWatcher = new FileTreeWatcher(root, new HashSet<>(), fileWatcherNotificationHandler);

        pm = new ProjectManager(vfsProvider, eventService, projectTypeRegistry, projectRegistry, projectHandlerRegistry,
                                importerRegistry, fileWatcherNotificationHandler, fileTreeWatcher);

        plugin = new ResourcesPlugin("target/index", wsPath, projectRegistry, pm);
        plugin.start();
        javaPlugin.start();

    }

    @AfterMethod
    public void shutdownMavenServer() throws Exception {
        mavenServerManager.shutdown();
    }

    protected FolderEntry createMultimoduleProject() throws ServerException, NotFoundException, ConflictException, ForbiddenException {
        String pom = "<groupId>test</groupId>" +
                     "<artifactId>testArtifact</artifactId>" +
                     "<version>42</version>" +
                     "<modules>" +
                     "    <module>module1</module>" +
                     "    <module>module2</module>" +
                     "</modules>";
        FolderEntry parentFolder = createTestProject("parent", pom);

        String pomModule1 = "<groupId>module1</groupId>" +
                            "<artifactId>testModule1</artifactId>" +
                            "<version>1</version>" +
                            "<dependencies>" +
                            "    <dependency>" +
                            "        <groupId>junit</groupId>" +
                            "        <artifactId>junit</artifactId>" +
                            "        <version>4.12</version>" +
                            "    </dependency>" +
                            "</dependencies>";
        createTestProject("parent/module1", pomModule1);

        String pomModule2 = "<groupId>module2</groupId>" +
                            "<artifactId>testModule2</artifactId>" +
                            "<version>2</version>" +
                            "<dependencies>" +
                            "    <dependency>" +
                            "        <groupId>junit</groupId>" +
                            "        <artifactId>junit</artifactId>" +
                            "        <version>4.12</version>" +
                            "    </dependency>" +
                            "</dependencies>";
        createTestProject("parent/module2", pomModule2);
        return parentFolder;
    }

    protected void createTestProjectWithPackages(String projectName, String pom, String... packages)
            throws ForbiddenException, ConflictException, NotFoundException, ServerException {
        FolderEntry testProject = createTestProject(projectName, pom);
        FolderEntry src = testProject.createFolder("src/main/java");
        for (String aPackage : packages) {
            src.createFolder(aPackage.replace(".", "/"));
        }
    }

    protected FolderEntry createTestProject(String name, String pomContent)
            throws ServerException, NotFoundException, ConflictException, ForbiddenException {
        FolderEntry folder = pm.getProjectsRoot().createFolder(name);
        folder.createFile("pom.xml", getPomContent(pomContent).getBytes());
        projectRegistry.setProjectType(folder.getPath().toString(),MAVEN_ID, false);

        //inform DeltaProcessingStat about new project
        JavaModelManager.getJavaModelManager().deltaState.resourceChanged(
                new ResourceChangedEvent(root, new ProjectCreatedEvent("", folder.getPath().toString())));

        return folder;
    }

    private String getPomContent(String content) {
        return "<?xml version=\"1.0\"?>" +
               "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"" +
               "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
               "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">" +
               "  <modelVersion>4.0.0</modelVersion>" +
               content +
               "</project>";
    }

    protected static class TestProjectType extends ProjectTypeDef {

        protected TestProjectType() {
            super("test", "test", true, true);
        }
    }

    protected static class MavenProjectType extends ProjectTypeDef {

        protected MavenProjectType() {
            super("maven", "maven", true, true);
        }
    }


}
