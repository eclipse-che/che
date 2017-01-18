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
import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadDump;
import org.eclipse.che.api.debug.shared.model.ThreadStatus;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.DisconnectEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.ResumeActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepIntoActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOutActionImpl;
import org.eclipse.che.api.debug.shared.model.impl.action.StepOverActionImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
public class JavaDebuggerTest {

    private Debugger                     debugger;
    private BlockingQueue<DebuggerEvent> events;


    @Test(priority = 10)
    public void testGetInfo() throws Exception {
        DebuggerInfo info = debugger.getInfo();

        assertEquals(info.getHost(), "localhost");
        assertEquals(info.getPort(), Integer.parseInt(System.getProperty("debug.port")));

        assertNotNull(info.getName());
        assertNotNull(info.getVersion());
    }

    @Test(priority = 20)
    public void testStartDebugger() throws Exception {
        BreakpointImpl breakpoint = new BreakpointImpl(new LocationImpl("com.HelloWorld", 17), false, null);
        debugger.start(new StartActionImpl(singletonList(breakpoint)));

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);

        Location location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getLineNumber(), 17);
        assertEquals(location.getTarget(), "com.HelloWorld");
    }

    @Test(priority = 30)
    public void testAddBreakpoint() throws Exception {
        int breakpointsCount = debugger.getAllBreakpoints().size();

        debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("com.HelloWorld", 18), false, null));

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

        Breakpoint breakpoint = ((BreakpointActivatedEvent)debuggerEvent).getBreakpoint();
        assertEquals(breakpoint.getLocation().getLineNumber(), 18);
        assertEquals(breakpoint.getLocation().getTarget(), "com.HelloWorld");
        assertTrue(breakpoint.isEnabled());

        assertEquals(debugger.getAllBreakpoints().size(), breakpointsCount + 1);
    }

    @Test(priority = 50, expectedExceptions = DebuggerException.class)
    public void testAddBreakpointToUnExistedLocation() throws Exception {
        debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("com.HelloWorld", 1), false, null));
    }

    @Test(priority = 60)
    public void testRemoveBreakpoint() throws Exception {
        debugger.deleteBreakpoint(new LocationImpl("com.HelloWorld", 17));
        assertEquals(debugger.getAllBreakpoints().size(), 1);
    }

    @Test(priority = 70)
    public void testRemoveUnExistedBreakpoint() throws Exception {
        int breakpointsCount = debugger.getAllBreakpoints().size();

        debugger.deleteBreakpoint(new LocationImpl("com.HelloWorld", 2));

        assertEquals(debugger.getAllBreakpoints().size(), breakpointsCount);
    }

    @Test(priority = 80)
    public void testGetAllBreakpoints() throws Exception {
        assertFalse(debugger.getAllBreakpoints().isEmpty());

        debugger.deleteAllBreakpoints();

        assertTrue(debugger.getAllBreakpoints().isEmpty());

        debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("com.HelloWorld", 18), false, null));

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);

        assertEquals(debugger.getAllBreakpoints().size(), 1);

        Breakpoint breakpoint = debugger.getAllBreakpoints().get(0);
        assertEquals(breakpoint.getLocation().getLineNumber(), 18);
        assertEquals(breakpoint.getLocation().getTarget(), "com.HelloWorld");
        assertTrue(breakpoint.isEnabled());
    }

    @Test(priority = 90)
    public void testSteps() throws Exception {
        debugger.deleteAllBreakpoints();

        debugger.addBreakpoint(new BreakpointImpl(new LocationImpl("com.HelloWorld", 20), false, null));

        assertTrue(events.take() instanceof BreakpointActivatedEvent);

        debugger.resume(new ResumeActionImpl());

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        Location location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 20);
        assertEquals(location.getExternalResourceId(), -1);
        assertEquals(location.getResourceProjectPath(), "/test");
        assertEquals(location.getResourcePath(), "/test/src/com/HelloWorld.java");

        debugger.stepInto(new StepIntoActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 28);

        debugger.stepOut(new StepOutActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 20);

        debugger.stepOver(new StepOverActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 21);

        debugger.stepOver(new StepOverActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 23);

        debugger.stepOver(new StepOverActionImpl());

        debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof SuspendEvent);
        location = ((SuspendEvent)debuggerEvent).getLocation();
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getLineNumber(), 24);
    }

    @Test(priority = 95)
    public void testGetThreadDumps() throws Exception {
        List<ThreadDump> threadDumps = debugger.getThreadDumps();

        Optional<ThreadDump> mainThread = threadDumps.stream().filter(t -> t.getName().equals("main")).findAny();
        assertTrue(mainThread.isPresent());

        ThreadDump threadDump = mainThread.get();
        assertEquals(threadDump.getName(), "main");
        assertEquals(threadDump.getGroupName(), "main");
        assertTrue(threadDump.isSuspended());
        assertEquals(threadDump.getStatus(), ThreadStatus.RUNNABLE);

        List<? extends StackFrameDump> frames = threadDump.getFrames();
        assertEquals(frames.size(), 1);

        StackFrameDump stackFrameDump = frames.get(0);

        List<? extends Variable> variables = stackFrameDump.getVariables();
        //TODO
        List<? extends Field> fields = stackFrameDump.getFields();
        //TODO

        Location location = stackFrameDump.getLocation();
        assertEquals(location.getLineNumber(), 24);
        assertEquals(location.getTarget(), "com.HelloWorld");
        assertEquals(location.getExternalResourceId(), -1);
        assertEquals(location.getResourceProjectPath(), "/test");
        assertEquals(location.getResourcePath(), "/test/src/com/HelloWorld.java");

        Method method = location.getMethod();
        assertEquals(method.getName(), "main");

        List<? extends Variable> arguments = method.getArguments();
        assertEquals(arguments.size(), 1);

        Variable variable = arguments.get(0);
        assertEquals(variable.getName(), "args");
        assertEquals(variable.getType(), "java.lang.String[]");
        assertEquals(variable.getVariablePath(), new VariablePathImpl("args"));

        //TODO
        SimpleValue value = variable.getValue();
        //TODO
        value.getString();
    }

    @Test(priority = 100)
    public void testEvaluateExpression() throws Exception {
        assertEquals(debugger.evaluate("2+2"), "4");
        assertEquals(debugger.evaluate("\"hello\""), "\"hello\"");
        assertEquals(debugger.evaluate("test"), "\"hello\"");
    }

    @Test(priority = 110)
    public void testSetAndGetValue() throws Exception {
        assertEquals(debugger.getValue(new VariablePathImpl("test")).getString(), "\"hello\"");
        assertEquals(debugger.getValue(new VariablePathImpl("msg")).getString(), "\"Hello, debugger!\"");

        debugger.setValue(new VariableImpl(new SimpleValueImpl("\"new hello\""), (new VariablePathImpl("test"))));

        assertEquals(debugger.getValue(new VariablePathImpl("test")).getString(), "\"new hello\"");

        StackFrameDump stackFrameDump = debugger.dumpStackFrame();
        Set<String> vars = stackFrameDump.getVariables().stream().map(Variable::getName).collect(Collectors.toSet());
        assertTrue(vars.contains("args"));
        assertTrue(vars.contains("msg"));
        assertTrue(vars.contains("test"));
    }

    @Test(priority = 120)
    public void testDisconnect() throws Exception {
        debugger.disconnect();

        DebuggerEvent debuggerEvent = events.take();
        assertTrue(debuggerEvent instanceof DisconnectEvent);
    }

    @BeforeClass
    protected void initProjectApi() throws Exception {
        TestWorkspaceHolder workspaceHolder = new TestWorkspaceHolder(new ArrayList<>());
        File root = new File("target/test-classes/workspace");
        assertTrue(root.exists());

        File indexDir = new File("target/fs_index");
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
                                                           workspaceHolder,
                                                           mock(FileWatcherManager.class));

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
