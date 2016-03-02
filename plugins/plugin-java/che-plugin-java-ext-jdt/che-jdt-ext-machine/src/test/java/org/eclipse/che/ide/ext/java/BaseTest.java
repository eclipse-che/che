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
package org.eclipse.che.ide.ext.java;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.ProjectRegistryImpl;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.WorkspaceHolder;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.impl.file.DefaultFileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.FileTreeWatcher;
import org.eclipse.che.api.vfs.impl.file.FileWatcherNotificationHandler;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.jdt.javadoc.JavaElementLinks;
import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * @author Evgen Vidolob
 */
// TODO: rework after new Project API
public abstract class BaseTest {

    protected static final String              wsPath        = BaseTest.class.getResource("/projects").getFile();
    private static final   String              workspacePath = BaseTest.class.getResource("/projects").getFile();
    protected static       Map<String, String> options       = new HashMap<>();
    protected static JavaProject project;
    protected static EventService    eventService      = new EventService();
    protected static ResourcesPlugin plugin            /*= new ResourcesPlugin("target/index", workspacePath,
                                                                             new DummyProjectManager(workspacePath, eventService))*/;
    protected static JavaPlugin      javaPlugin        = new JavaPlugin(wsPath + "/set");
    protected static FileBuffersPlugin
                                     fileBuffersPlugin = new FileBuffersPlugin();
    protected final static String INDEX_PATH = wsPath + "/fs_index";

    protected static TestWorkspaceHolder workspaceHolder;

    protected static File root;

    protected static ProjectManager pm;

    protected static LocalVirtualFileSystemProvider vfsProvider;

    protected static ProjectRegistryImpl projectRegistry;

    protected static FileWatcherNotificationHandler fileWatcherNotificationHandler;

    protected static FileTreeWatcher fileTreeWatcher;

    protected static ProjectTypeRegistry projectTypeRegistry;

    protected static ProjectHandlerRegistry projectHandlerRegistry;

    protected static ProjectImporterRegistry importerRegistry;

    static {


        try {
            if (workspaceHolder == null)
                workspaceHolder = new TestWorkspaceHolder();

            if (root == null)
                root = new File(wsPath);

            File indexDir = new File(INDEX_PATH);

            Set<PathMatcher> filters = new HashSet<>();
            filters.add(path -> true);
            FSLuceneSearcherProvider sProvider = new FSLuceneSearcherProvider(indexDir, filters);

            vfsProvider = new LocalVirtualFileSystemProvider(root, sProvider);


            projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());
            projectTypeRegistry.registerProjectType(new TestProjectType());

            projectHandlerRegistry = new ProjectHandlerRegistry(new HashSet<>());

            projectRegistry = new ProjectRegistryImpl(workspaceHolder, vfsProvider, projectTypeRegistry, projectHandlerRegistry);
            projectRegistry.initProjects();

            importerRegistry = new ProjectImporterRegistry(new HashSet<>());

            fileWatcherNotificationHandler = new DefaultFileWatcherNotificationHandler(vfsProvider);
            fileTreeWatcher = new FileTreeWatcher(root, new HashSet<>(), fileWatcherNotificationHandler);

            pm = new ProjectManager(vfsProvider, eventService, projectTypeRegistry, projectRegistry, projectHandlerRegistry,
                                    importerRegistry, fileWatcherNotificationHandler, fileTreeWatcher);
            plugin = new ResourcesPlugin("target/index", workspacePath, projectRegistry, pm);
            plugin.start();
            javaPlugin.start();
        } catch (Throwable e){
            e.printStackTrace();
        }
    }




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

    protected static String getHandldeForRtJarStart() {
        String javaHome = System.getProperty("java.home") + "/lib/rt.jar";
        javaHome = javaHome.replaceAll("/", "\\\\/");
        return String.valueOf(JavaElementLinks.LINK_SEPARATOR) + "=test/" + javaHome;
    }

    @Before
    public void setUp() throws Exception {
        project = (JavaProject)JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject("/test");
    }

    @After
    public void closeProject() throws Exception {
        if (project != null) {
            project.close();
        }
        File pref = new File(wsPath + "/test/.codenvy/project.preferences");
        if (pref.exists()) {
            pref.delete();
        }

    }

    protected static class TestProjectType extends ProjectTypeDef {

        protected TestProjectType() {
            super("test", "test", true, true);
        }
    }

    protected static class TestWorkspaceHolder extends WorkspaceHolder {

        //ArrayList <RegisteredProject> updatedProjects = new ArrayList<>();

        protected TestWorkspaceHolder() throws ServerException {
            super(DtoFactory.newDto(UsersWorkspaceDto.class).withId("id")
                            .withConfig(DtoFactory.newDto(WorkspaceConfigDto.class)
                                                  .withName("name")));
        }


        protected TestWorkspaceHolder(List<ProjectConfigDto> projects) throws ServerException {
            super(DtoFactory.newDto(UsersWorkspaceDto.class)
                            .withId("id")
                            .withConfig(DtoFactory.newDto(WorkspaceConfigDto.class)
                                                  .withName("name")
                                                  .withProjects(projects)));
        }

        @Override
        public void updateProjects(Collection<RegisteredProject> projects) throws ServerException {
            List<RegisteredProject> persistedProjects = projects.stream().filter(project -> !project.isDetected()).collect(toList());
            workspace.setProjects(persistedProjects);
            //setProjects(new ArrayList<>(projects));
        }
    }

}
