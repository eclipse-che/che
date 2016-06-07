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
package org.eclipse.che.ide.debug;

import com.google.gwt.storage.client.Storage;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.Breakpoint;
import org.eclipse.che.ide.api.debug.Breakpoint.Type;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.BreakpointManagerObservable;
import org.eclipse.che.ide.api.debug.BreakpointManagerObserver;
import org.eclipse.che.ide.api.debug.BreakpointRenderer;
import org.eclipse.che.ide.api.debug.BreakpointRenderer.LineChangeAction;
import org.eclipse.che.ide.api.debug.HasBreakpointRenderer;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorOpenedEventHandler;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.api.event.project.DeleteProjectEvent;
import org.eclipse.che.ide.api.event.project.DeleteProjectHandler;
import org.eclipse.che.ide.api.project.node.HasProjectConfig.ProjectConfig;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.project.tree.VirtualFileImpl;
import org.eclipse.che.ide.api.project.tree.VirtualFileInfo;
import org.eclipse.che.ide.debug.dto.BreakpointDto;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.project.event.ProjectExplorerLoadedEvent;
import org.eclipse.che.ide.project.event.ResourceNodeDeletedEvent;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ItemReferenceBasedNode;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.resource.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Implementation of {@link BreakpointManager} for editor.
 *
 * @author Anatoliy Bazko
 * @author Valeriy Svydenko
 * @author Dmytro Nochevnov
 */
public class BreakpointManagerImpl implements BreakpointManager,
                                              LineChangeAction,
                                              BreakpointManagerObservable,
                                              DebuggerManagerObserver {

    private static final Logger LOG                           = Logger.getLogger(BreakpointManagerImpl.class.getName());
    private static final String LOCAL_STORAGE_BREAKPOINTS_KEY = "che-breakpoints";

    private final Map<String, List<Breakpoint>>   breakpoints;
    private final EditorAgent                     editorAgent;
    private final DebuggerManager                 debuggerManager;
    private final DtoFactory                      dtoFactory;
    private final List<BreakpointManagerObserver> observers;

    private Breakpoint currentBreakpoint;

    @Inject
    public BreakpointManagerImpl(final EditorAgent editorAgent,
                                 final DebuggerManager debuggerManager,
                                 final EventBus eventBus,
                                 final DtoFactory dtoFactory) {
        this.editorAgent = editorAgent;
        this.breakpoints = new HashMap<>();
        this.debuggerManager = debuggerManager;
        this.dtoFactory = dtoFactory;
        this.observers = new ArrayList<>();

        this.debuggerManager.addObserver(this);
        registerEventHandlers(eventBus);
        restoreBreakpoints();
    }

    @Override
    public void changeBreakpointState(final int lineNumber) {
        EditorPartPresenter editor = editorAgent.getActiveEditor();
        if (editor == null) {
            return;
        }

        final VirtualFile activeFile = editor.getEditorInput().getFile();

        List<Breakpoint> pathBreakpoints = breakpoints.get(activeFile.getPath());
        if (pathBreakpoints != null) {
            for (final Breakpoint breakpoint : pathBreakpoints) {
                if (breakpoint.getLineNumber() == lineNumber) {
                    // breakpoint already exists at given line
                    deleteBreakpoint(activeFile, breakpoint);
                    return;
                }
            }
        }

        if (isLineNotEmpty(activeFile, lineNumber)) {
            Breakpoint breakpoint = new Breakpoint(Type.BREAKPOINT,
                                                   lineNumber,
                                                   activeFile.getPath(),
                                                   activeFile,
                                                   false);
            addBreakpoint(breakpoint);
        }
    }

    /**
     * Deletes breakpoint from the list and JVM.
     * Removes breakpoint mark.
     */
    private void deleteBreakpoint(final VirtualFile activeFile,
                                  final Breakpoint breakpoint) {
        doDeleteBreakpoint(breakpoint);
        for (BreakpointManagerObserver observer : observers) {
            observer.onBreakpointDeleted(breakpoint);
        }

        Debugger debugger = debuggerManager.getActiveDebugger();
        if (debugger != null) {
            debugger.deleteBreakpoint(activeFile, breakpoint.getLineNumber());
        }
    }

    /**
     * Deletes breakpoint from the list.
     */
    private void doDeleteBreakpoint(Breakpoint breakpoint) {
        BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getPath());
        if (breakpointRenderer != null) {
            breakpointRenderer.removeBreakpointMark(breakpoint.getLineNumber());
        }

        String path = breakpoint.getPath();

        List<Breakpoint> pathBreakpoints = breakpoints.get(path);
        if (pathBreakpoints != null) {
            pathBreakpoints.remove(breakpoint);
            if (pathBreakpoints.isEmpty()) {
                breakpoints.remove(breakpoint.getPath());
            }
        }

        preserveBreakpoints();
    }

    /**
     * Deletes breakpoints linked to paths from the list and JVM.
     * Removes breakpoints' marks.
     */
    private void deleteBreakpoints(final Set<String> paths) {
        for (String path : paths) {
            List<Breakpoint> breakpointsToDelete = breakpoints.get(path);
            if (breakpointsToDelete != null) {
                for (Breakpoint breakpoint : new ArrayList<>(breakpointsToDelete)) {
                    deleteBreakpoint(breakpoint.getFile(), breakpoint);
                }
            }
        }
    }

    /**
     * Adds breakpoint to the list and JVM.
     */
    private void addBreakpoint(final Breakpoint breakpoint) {
        List<Breakpoint> pathBreakpoints = breakpoints.get(breakpoint.getPath());
        if (pathBreakpoints == null) {
            pathBreakpoints = new ArrayList<>();
            breakpoints.put(breakpoint.getPath(), pathBreakpoints);
        }

        if (!pathBreakpoints.contains(breakpoint)) {
            pathBreakpoints.add(breakpoint);
        }
        preserveBreakpoints();

        final BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getPath());
        if (breakpointRenderer != null) {
            breakpointRenderer.addBreakpointMark(breakpoint.getLineNumber(), new LineChangeAction() {
                @Override
                public void onLineChange(VirtualFile file, int firstLine, int linesAdded, int linesRemoved) {
                    BreakpointManagerImpl.this.onLineChange(file, firstLine, linesAdded, linesRemoved);
                }
            });
            breakpointRenderer.setBreakpointActive(breakpoint.getLineNumber(), breakpoint.isActive());
        }

        for (BreakpointManagerObserver observer : observers) {
            observer.onBreakpointAdded(breakpoint);
        }

        Debugger debugger = debuggerManager.getActiveDebugger();
        if (debugger != null) {
            debugger.addBreakpoint(breakpoint.getFile(), breakpoint.getLineNumber());
        }
    }

    /**
     * Indicates if line of code to add breakpoint at is executable.
     */
    private boolean isLineNotEmpty(final VirtualFile activeFile, int lineNumber) {
        EditorPartPresenter editor = getEditorForFile(activeFile.getPath());
        if (editor instanceof TextEditorPresenter) {
            Document document = ((TextEditorPresenter)editor).getDocument();
            return !document.getLineContent(lineNumber).trim().isEmpty();
        }

        return false;
    }

    private void removeBreakpointsForPath(final List<Breakpoint> pathBreakpoints) {
        for (final Breakpoint breakpoint : pathBreakpoints) {
            BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getPath());
            if (breakpointRenderer != null) {
                breakpointRenderer.removeBreakpointMark(breakpoint.getLineNumber());
            }
        }
    }

    @Override
    public List<Breakpoint> getBreakpointList() {
        final List<Breakpoint> result = new ArrayList<>();
        for (final List<Breakpoint> fileBreakpoints : breakpoints.values()) {
            result.addAll(fileBreakpoints);
        }
        return result;
    }

    private void setCurrentBreakpoint(String filePath, int lineNumber) {
        deleteCurrentBreakpoint();

        EditorPartPresenter editor = getEditorForFile(filePath);
        if (editor != null) {
            VirtualFile activeFile = editor.getEditorInput().getFile();
            doSetCurrentBreakpoint(activeFile, lineNumber);
        }

    }

    private void doSetCurrentBreakpoint(VirtualFile activeFile, int lineNumber) {
        currentBreakpoint = new Breakpoint(Type.CURRENT, lineNumber, activeFile.getPath(), activeFile, true);

        BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(activeFile.getPath());
        if (breakpointRenderer != null) {
            breakpointRenderer.setLineActive(lineNumber, true);
        }

        preserveBreakpoints();
    }

    @Override
    public void deleteAllBreakpoints() {
        for (List<Breakpoint> pathBreakpoints : breakpoints.values()) {
            removeBreakpointsForPath(pathBreakpoints);
        }
        breakpoints.clear();
        preserveBreakpoints();

        for (BreakpointManagerObserver observer : observers) {
            observer.onAllBreakpointsDeleted();
        }

        Debugger debugger = debuggerManager.getActiveDebugger();
        if (debugger != null) {
            debugger.deleteAllBreakpoints();
        }
    }

    public void deleteCurrentBreakpoint() {
        if (currentBreakpoint != null) {
            int oldLineNumber = currentBreakpoint.getLineNumber();
            BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(currentBreakpoint.getPath());
            if (breakpointRenderer != null) {
                breakpointRenderer.setLineActive(oldLineNumber, false);
            }

            currentBreakpoint = null;
        }

        preserveBreakpoints();
    }

    @Nullable
    private EditorPartPresenter getEditorForFile(String path) {
        return editorAgent.getOpenedEditor(Path.valueOf(path));
    }

    @Nullable
    private BreakpointRenderer getBreakpointRendererForFile(String path) {
        final EditorPartPresenter editor = getEditorForFile(path);
        if (editor != null) {
            return getBreakpointRendererForEditor(editor);
        } else {
            return null;
        }
    }

    @Nullable
    private BreakpointRenderer getBreakpointRendererForEditor(final EditorPartPresenter editor) {
        if (editor instanceof HasBreakpointRenderer) {
            final BreakpointRenderer renderer = ((HasBreakpointRenderer)editor).getBreakpointRenderer();
            if (renderer != null && renderer.isReady()) {
                return renderer;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLineChange(final VirtualFile file, final int firstLine, final int linesAdded, final int linesRemoved) {
        final List<Breakpoint> fileBreakpoints = breakpoints.get(file.getPath());
        final int delta = linesAdded - linesRemoved;

        if (fileBreakpoints != null) {
            LOG.fine("Change in file with breakpoints " + file.getPath());

            final List<Breakpoint> toRemove = new ArrayList<>();
            final List<Breakpoint> toAdd = new ArrayList<>();

            for (final Breakpoint breakpoint : fileBreakpoints) {
                final int lineNumber = breakpoint.getLineNumber();
                if (lineNumber < firstLine) {
                    // we're before any change
                    continue;
                }

                toRemove.add(breakpoint);
                toAdd.add(new Breakpoint(breakpoint.getType(),
                                         breakpoint.getLineNumber() + delta,
                                         breakpoint.getPath(),
                                         breakpoint.getFile(),
                                         breakpoint.isActive()));
            }


            for (final Breakpoint breakpoint : toRemove) {
                deleteBreakpoint(file, breakpoint);
            }
            for (final Breakpoint breakpoint : toAdd) {
                if (isLineNotEmpty(file, breakpoint.getLineNumber())) {
                    addBreakpoint(new Breakpoint(breakpoint.getType(),
                                                 breakpoint.getLineNumber(),
                                                 breakpoint.getPath(),
                                                 file,
                                                 false));
                }
            }
        }
    }

    /**
     * Registers events handlers.
     */
    private void registerEventHandlers(EventBus eventBus) {
        eventBus.addHandler(EditorOpenedEvent.TYPE, new EditorOpenedEventHandler() {
            @Override
            public void onEditorOpened(EditorOpenedEvent event) {
                onOpenEditor(event.getFile().getPath(), event.getEditor());
            }
        });

        eventBus.addHandler(DeleteProjectEvent.TYPE, new DeleteProjectHandler() {
            @Override
            public void onProjectDeleted(DeleteProjectEvent event) {
                if (breakpoints.isEmpty()) {
                    return;
                }

                ProjectConfigDto config = event.getProjectConfig();
                String path = config.getPath() + "/";
                deleteBreakpoints(getBreakpointPaths(path));
            }
        });

        eventBus.addHandler(ResourceNodeDeletedEvent.getType(), new ResourceNodeDeletedEvent.ResourceNodeDeletedHandler() {
            @Override
            public void onResourceEvent(ResourceNodeDeletedEvent event) {
                if (breakpoints.isEmpty()) {
                    return;
                }

                ResourceBasedNode node = event.getNode();
                if (node instanceof ItemReferenceBasedNode) {
                    String path = ((ItemReferenceBasedNode)node).getStorablePath();

                    if (node instanceof FolderReferenceNode) {
                        path += "/";
                        deleteBreakpoints(getBreakpointPaths(path));

                    } else if (node instanceof FileReferenceNode) {
                        deleteBreakpoints(Collections.singleton(path));
                    }
                }
            }
        });

        eventBus.addHandler(ProjectExplorerLoadedEvent.getType(), new ProjectExplorerLoadedEvent.ProjectExplorerLoadedHandler() {
            @Override
            public void onProjectsLoaded(ProjectExplorerLoadedEvent event) {
                if (breakpoints.isEmpty()) {
                    return;
                }

                // remove breakpoints which refer to un-existed projects
                List<Node> projects = event.getNodes();
                Set<String> pathsToDelete = new HashSet<>(breakpoints.keySet());

                for (String breakpointPath : breakpoints.keySet()) {
                    for (Node project : projects) {
                        String projectName = project.getName();
                        if (breakpointPath.startsWith("/" + projectName + "/")) {
                            pathsToDelete.remove(breakpointPath);
                            break;
                        }
                    }
                }

                deleteBreakpoints(pathsToDelete);
            }
        });
    }

    /**
     * @param pathToFind
     *         examples: "/test-spring/", "/test-spring/src/", "/test-spring/src/main/java/Test.java"
     * @return set of breakpoint paths which related to pathToFind
     */
    private Set<String> getBreakpointPaths(String pathToFind) {
        Set<String> foundPaths = new HashSet<>(breakpoints.size());
        for (Entry<String, List<Breakpoint>> breakpointsForPath : breakpoints.entrySet()) {
            String path = breakpointsForPath.getKey();
            if (path.startsWith(pathToFind)) {
                foundPaths.add(path);
            }
        }

        return foundPaths;
    }

    /**
     * The new file has been opened in the editor.
     * Method reads breakpoints.
     */
    private void onOpenEditor(String path, EditorPartPresenter editor) {
        final List<Breakpoint> fileBreakpoints = breakpoints.get(path);

        if (fileBreakpoints != null) {
            final BreakpointRenderer breakpointRenderer = getBreakpointRendererForEditor(editor);

            if (breakpointRenderer != null) {
                for (final Breakpoint breakpoint : fileBreakpoints) {
                    reAddBreakpointMark(breakpointRenderer, breakpoint);
                }
            }
        }

        if (currentBreakpoint != null && path.equals(currentBreakpoint.getPath())) {
            BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(path);
            if (breakpointRenderer != null) {
                breakpointRenderer.setLineActive(currentBreakpoint.getLineNumber(), true);
            }
        }
    }

    private void reAddBreakpointMark(BreakpointRenderer breakpointRenderer, Breakpoint breakpoint) {
        int lineNumber = breakpoint.getLineNumber();

        breakpointRenderer.addBreakpointMark(lineNumber, new LineChangeAction() {
            @Override
            public void onLineChange(VirtualFile file, int firstLine, int linesAdded, int linesRemoved) {
                BreakpointManagerImpl.this.onLineChange(file, firstLine, linesAdded, linesRemoved);
            }
        });
        breakpointRenderer.setBreakpointActive(lineNumber, breakpoint.isActive());
    }

    private void preserveBreakpoints() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            List<BreakpointDto> allDtoBreakpoints = new LinkedList<BreakpointDto>();

            List<Breakpoint> allBreakpoints = getBreakpointList();
            if (currentBreakpoint != null) {
                allBreakpoints.add(currentBreakpoint);
            }

            for (Breakpoint breakpoint : allBreakpoints) {
                BreakpointDto dto = dtoFactory.createDto(BreakpointDto.class);
                dto.setType(breakpoint.getType());
                dto.setPath(breakpoint.getPath());
                dto.setLineNumber(breakpoint.getLineNumber());
                dto.setFileProjectConfig(breakpoint.getFile().getProject().getProjectConfig());
                dto.setActive(breakpoint.isActive());

                allDtoBreakpoints.add(dto);
            }

            String data = dtoFactory.toJson(allDtoBreakpoints);
            localStorage.setItem(LOCAL_STORAGE_BREAKPOINTS_KEY, data);
        }
    }

    private void restoreBreakpoints() {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage == null) {
            return;
        }

        String data = localStorage.getItem(LOCAL_STORAGE_BREAKPOINTS_KEY);
        if (data == null || data.isEmpty()) {
            return;
        }

        List<BreakpointDto> allDtoBreakpoints = dtoFactory.createListDtoFromJson(data, BreakpointDto.class);

        for (final BreakpointDto dto : allDtoBreakpoints) {
            VirtualFileInfo virtualFileInfo = VirtualFileInfo.newBuilder()
                                                             .setPath(dto.getPath())
                                                             .setProject(new ProjectConfig(dto.getFileProjectConfig()))
                                                             .build();

            VirtualFile file = new VirtualFileImpl(virtualFileInfo);
            if (dto.getType() == Type.CURRENT) {
                doSetCurrentBreakpoint(file, dto.getLineNumber());
            } else {
                addBreakpoint(new Breakpoint(dto.getType(),
                                             dto.getLineNumber(),
                                             dto.getPath(),
                                             file,
                                             dto.isActive()));
            }
        }
    }

    // Debugger events

    @Override
    public void onActiveDebuggerChanged(@Nullable Debugger activeDebugger) {}

    @Override
    public void onDebuggerAttached(DebuggerDescriptor debuggerDescriptor, Promise<Void> connect) { }

    @Override
    public void onDebuggerDisconnected() {
        for (Entry<String, List<Breakpoint>> entry : breakpoints.entrySet()) {
            List<Breakpoint> breakpointsForPath = entry.getValue();

            for (int i = 0; i < breakpointsForPath.size(); i++) {
                Breakpoint breakpoint = breakpointsForPath.get(i);

                if (breakpoint.isActive()) {
                    Breakpoint newInactiveBreakpoint = new Breakpoint(breakpoint.getType(),
                                                                      breakpoint.getLineNumber(),
                                                                      breakpoint.getPath(),
                                                                      breakpoint.getFile(),
                                                                      false);
                    breakpointsForPath.set(i, newInactiveBreakpoint);

                    BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getPath());
                    if (breakpointRenderer != null) {
                        breakpointRenderer.setBreakpointActive(breakpoint.getLineNumber(), false);
                    }
                }
            }
        }

        deleteCurrentBreakpoint();
    }

    @Override
    public void onBreakpointAdded(Breakpoint breakpoint) {
        String path = breakpoint.getPath();
        List<Breakpoint> breakpointsForPath = breakpoints.get(path);
        if (breakpointsForPath == null) {
            breakpointsForPath = new ArrayList<>();
            breakpoints.put(path, breakpointsForPath);
        }

        int i = breakpointsForPath.indexOf(breakpoint);
        if (i == -1) {
            breakpointsForPath.add(breakpoint);
        } else {
            breakpointsForPath.set(i, breakpoint);
        }

        BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getPath());
        if (breakpointRenderer != null) {
            breakpointRenderer.setBreakpointActive(breakpoint.getLineNumber(), breakpoint.isActive());
        }

        preserveBreakpoints();
    }

    @Override
    public void onBreakpointActivated(String filePath, int lineNumber) {
        List<Breakpoint> breakpointsForPath = breakpoints.get(filePath);
        if (breakpointsForPath == null) {
            return;
        }

        for (int i = 0; i < breakpointsForPath.size(); i++) {
            Breakpoint breakpoint = breakpointsForPath.get(i);

            if (breakpoint.getLineNumber() == lineNumber) {
                Breakpoint newActiveBreakpoint = new Breakpoint(breakpoint.getType(),
                                                                breakpoint.getLineNumber(),
                                                                breakpoint.getPath(),
                                                                breakpoint.getFile(),
                                                                true);
                breakpointsForPath.set(i, newActiveBreakpoint);
                preserveBreakpoints();

                BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getPath());
                if (breakpointRenderer != null) {
                    breakpointRenderer.setBreakpointActive(breakpoint.getLineNumber(), true);
                }
            }
        }
    }

    @Override
    public void onBreakpointDeleted(Breakpoint breakpoint) { }

    @Override
    public void onAllBreakpointsDeleted() {}

    @Override
    public void onPreStepInto() {
        deleteCurrentBreakpoint();
    }

    @Override
    public void onPreStepOut() {
        deleteCurrentBreakpoint();
    }

    @Override
    public void onPreStepOver() {
        deleteCurrentBreakpoint();
    }

    @Override
    public void onPreResume() {
        deleteCurrentBreakpoint();
    }

    @Override
    public void onBreakpointStopped(String filePath, String className, int lineNumber) {
        setCurrentBreakpoint(filePath, lineNumber - 1);
    }

    @Override
    public void onValueChanged(List<String> path, String newValue) { }

    @Override
    public void addObserver(BreakpointManagerObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(BreakpointManagerObserver observer) {
        observers.remove(observer);
    }
}
