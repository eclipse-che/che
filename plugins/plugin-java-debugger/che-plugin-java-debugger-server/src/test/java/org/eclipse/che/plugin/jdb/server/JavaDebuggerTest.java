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
package org.eclipse.che.plugin.jdb.server;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.api.debugger.server.Debugger;
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
import org.eclipse.che.api.vfs.search.impl.FSLuceneSearcherProvider;
import org.eclipse.che.api.vfs.watcher.FileWatcherManager;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.plugin.java.server.projecttype.JavaProjectType;
import org.eclipse.che.plugin.java.server.projecttype.JavaValueProviderFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
public class JavaDebuggerTest {

    private Debugger                     debugger;
    private BlockingQueue<DebuggerEvent> events;

    private static final String WS_PATH    = "target/test-classes/workspace";
    private static final String INDEX_PATH = "target/fs_index";

    @BeforeClass
    protected void initProjectApi() throws Exception {
        TestWorkspaceHolder workspaceHolder = new TestWorkspaceHolder();
        File root = new File(WS_PATH);
        assertTrue(root.exists());

        File indexDir = new File(INDEX_PATH);
        assertTrue(indexDir.mkdirs());

        Set<PathMatcher> filters = new HashSet<>();
        filters.add(path -> true);
        FSLuceneSearcherProvider sProvider = new FSLuceneSearcherProvider(indexDir, filters);

        EventService eventService = new EventService();
        LocalVirtualFileSystemProvider vfsProvider = new LocalVirtualFileSystemProvider(root, sProvider);
        ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());
        projectTypeRegistry.registerProjectType(new JavaProjectType(new JavaValueProviderFactory()));
        ProjectHandlerRegistry projectHandlerRegistry = new ProjectHandlerRegistry(new HashSet<>());
        ProjectRegistry projectRegistry = new ProjectRegistry(workspaceHolder,
                                                              vfsProvider,
                                                              projectTypeRegistry,
                                                              projectHandlerRegistry,
                                                              eventService);
        projectRegistry.initProjects();

        ProjectImporterRegistry importerRegistry = new ProjectImporterRegistry(new HashSet<>());
        FileWatcherNotificationHandler fileWatcherNotificationHandler = new DefaultFileWatcherNotificationHandler(vfsProvider);
        FileTreeWatcher fileTreeWatcher = new FileTreeWatcher(root, new HashSet<>(), fileWatcherNotificationHandler);
        ProjectManager projectManager = new ProjectManager(vfsProvider,
                                                           eventService,
                                                           projectTypeRegistry,
                                                           projectRegistry,
                                                           projectHandlerRegistry,
                                                           importerRegistry,
                                                           fileWatcherNotificationHandler,
                                                           fileTreeWatcher,
                                                           new TestWorkspaceHolder(new ArrayList<>()), mock(FileWatcherManager.class));

        ResourcesPlugin resourcesPlugin =
                new ResourcesPlugin("target/index",
                                    root.getAbsolutePath(),
                                    () -> projectRegistry,
                                    () -> projectManager);
        resourcesPlugin.start();

        JavaPlugin javaPlugin = new JavaPlugin(root.getAbsolutePath() + "/.settings", resourcesPlugin, projectRegistry);
        javaPlugin.start();

        projectRegistry.setProjectType("test", "java", false);

        JavaModelManager.getDeltaState().initializeRoots(true);

        events = new ArrayBlockingQueue<>(10);
        Map<String, String> connectionProperties = ImmutableMap.of("host", "localhost",
                                                                   "port", System.getProperty("debug.port"));

        JavaDebuggerFactory factory = new JavaDebuggerFactory();
        debugger = factory.create(connectionProperties, events::add);
    }


    @Test(priority = 1)
    public void testGetInfo() throws Exception {
        DebuggerInfo info = debugger.getInfo();

        assertEquals(info.getHost(), "localhost");
        assertEquals(info.getPort(), Integer.parseInt(System.getProperty("debug.port")));

        assertNotNull(info.getName());
        assertNotNull(info.getVersion());
    }

    @Test(priority = 2)
    public void testStartDebugger() throws Exception {
        BreakpointImpl breakpoint = new BreakpointImpl(new LocationImpl("com.HelloWorld", 16), false, null);
        StartActionImpl startAction = new StartActionImpl(Collections.singletonList(breakpoint));
        debugger.start(startAction);

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent, "Debugger event: " + debuggerEvent.getClass().getName());

        BreakpointActivatedEvent breakpointActivatedEvent = (BreakpointActivatedEvent)debuggerEvent;
        Location location = breakpointActivatedEvent.getBreakpoint().getLocation();
        assertEquals(location.getLineNumber(), 16);
        assertEquals(location.getTarget(), "com.HelloWorld");

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent, "Debugger event: " + debuggerEvent.getClass().getName());
        SuspendEvent suspendEvent = (SuspendEvent)debuggerEvent;

        location = suspendEvent.getLocation();
        assertEquals(location.getLineNumber(), 16);
        assertEquals(location.getTarget(), "com.HelloWorld");
    }

    @Test(priority = 3)
    public void testAddBreakpoint() throws Exception {
        debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("com.HelloWorld", 17), false, null));

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent, "Debugger event: " + debuggerEvent.getClass().getName());

        BreakpointActivatedEvent breakpointActivatedEvent = (BreakpointActivatedEvent)debuggerEvent;
        Location location = breakpointActivatedEvent.getBreakpoint().getLocation();
        assertEquals(location.getLineNumber(), 17);
        assertEquals(location.getTarget(), "com.HelloWorld");

        List<Breakpoint> breakpoints = debugger.getAllBreakpoints();
        assertEquals(breakpoints.size(), 2);
    }

    @Test(priority = 4)
    public void testRemoveBreakpoint() throws Exception {
//        debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("HelloWorld", 5), false, null));
//
//        DebuggerEvent debuggerEvent = events.take();
//        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent, "Debugger event: " + debuggerEvent.getClass().getName());
//
//        BreakpointActivatedEvent breakpointActivatedEvent = (BreakpointActivatedEvent)debuggerEvent;
//        Location location = breakpointActivatedEvent.getBreakpoint().getLocation();
//        assertEquals(location.getLineNumber(), 4);
//        assertEquals(location.getTarget(), "HelloWorld");
//
//        List<Breakpoint> breakpoints = debugger.getAllBreakpoints();
//        assertEquals(breakpoints.size(), 2);
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
