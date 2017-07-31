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
package org.eclipse.che.ide.debug;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.Breakpoint;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.BreakpointManagerObservable;
import org.eclipse.che.ide.api.debug.BreakpointManagerObserver;
import org.eclipse.che.ide.api.debug.BreakpointRenderer;
import org.eclipse.che.ide.api.debug.BreakpointRenderer.LineChangeAction;
import org.eclipse.che.ide.api.debug.BreakpointStorage;
import org.eclipse.che.ide.api.debug.HasBreakpointRenderer;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.event.project.DeleteProjectEvent;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.resource.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import static org.eclipse.che.ide.api.debug.Breakpoint.Type.BREAKPOINT;

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

    private static final Logger LOG = Logger.getLogger(BreakpointManagerImpl.class.getName());

    private final Map<String, List<Breakpoint>>   breakpoints;
    private final EditorAgent                     editorAgent;
    private final DebuggerManager                 debuggerManager;
    private final BreakpointStorage               breakpointStorage;
    private final List<BreakpointManagerObserver> observers;

    private Breakpoint currentBreakpoint;

    @Inject
    public BreakpointManagerImpl(EditorAgent editorAgent,
                                 DebuggerManager debuggerManager,
                                 EventBus eventBus,
                                 BreakpointStorage breakpointStorage) {
        this.editorAgent = editorAgent;
        this.breakpointStorage = breakpointStorage;
        this.breakpoints = new HashMap<>();
        this.debuggerManager = debuggerManager;
        this.observers = new ArrayList<>();

        this.debuggerManager.addObserver(this);
        registerEventHandlers(eventBus);
    }

    @Override
    public void changeBreakpointState(final int lineNumber) {
        EditorPartPresenter editor = editorAgent.getActiveEditor();
        if (editor == null) {
            return;
        }

        final VirtualFile activeFile = editor.getEditorInput().getFile();

        List<Breakpoint> pathBreakpoints = breakpoints.get(activeFile.getLocation().toString());
        if (pathBreakpoints != null) {
            for (final Breakpoint breakpoint : pathBreakpoints) {
                if (breakpoint.getLineNumber() == lineNumber) {
                    // breakpoint already exists at given line
                    breakpointStorage.delete(breakpoint);
                    deleteBreakpoint(activeFile, breakpoint);
                    return;
                }
            }
        }

        if (isLineNotEmpty(activeFile, lineNumber)) {
            Breakpoint breakpoint = new Breakpoint(BREAKPOINT,
                                                   lineNumber,
                                                   activeFile.getLocation().toString(),
                                                   activeFile,
                                                   false);
            addBreakpoint(breakpoint);
            breakpointStorage.add(breakpoint);
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
    }

    /**
     * Deletes breakpoints linked to paths from the list and JVM.
     * Removes breakpoints' marks.
     */
    private void deleteBreakpoints(final Set<String> paths) {
        for (String path : paths) {
            if (!breakpoints.containsKey(path)) {
                continue;
            }

            List<Breakpoint> breakpointsToDelete = new ArrayList<>(breakpoints.get(path));
            breakpointStorage.deleteAll(breakpointsToDelete);
            breakpointsToDelete.forEach(breakpoint -> deleteBreakpoint(breakpoint.getFile(), breakpoint));
        }
    }

    /**
     * Adds breakpoint to the list and JVM.
     */
    private void addBreakpoint(final Breakpoint breakpoint) {
        List<Breakpoint> pathBreakpoints = breakpoints.computeIfAbsent(breakpoint.getPath(), k -> new ArrayList<>());
        if (!pathBreakpoints.contains(breakpoint)) {
            pathBreakpoints.add(breakpoint);
        }

        final BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getPath());
        if (breakpointRenderer != null) {
            breakpointRenderer.addBreakpointMark(breakpoint.getLineNumber(), BreakpointManagerImpl.this::onLineChange);
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
        EditorPartPresenter editor = getEditorForFile(activeFile.getLocation().toString());
        if (editor instanceof TextEditor) {
            Document document = ((TextEditor)editor).getDocument();
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
        currentBreakpoint = new Breakpoint(Breakpoint.Type.CURRENT, lineNumber, filePath, null, true);

        EditorPartPresenter editor = getEditorForFile(filePath);
        if (editor != null) {
            VirtualFile activeFile = editor.getEditorInput().getFile();
            doSetCurrentBreakpoint(activeFile, lineNumber);
        }

    }

    private void doSetCurrentBreakpoint(VirtualFile activeFile, int lineNumber) {
        BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(activeFile.getLocation().toString());
        if (breakpointRenderer != null) {
            breakpointRenderer.setLineActive(lineNumber, true);
        }
    }

    @Override
    public void deleteAllBreakpoints() {
        for (List<Breakpoint> pathBreakpoints : breakpoints.values()) {
            removeBreakpointsForPath(pathBreakpoints);
        }

        breakpointStorage.clear();
        breakpoints.clear();

        for (BreakpointManagerObserver observer : observers) {
            observer.onAllBreakpointsDeleted();
        }

        Debugger debugger = debuggerManager.getActiveDebugger();
        if (debugger != null) {
            debugger.deleteAllBreakpoints();
        }
    }

    private void deleteCurrentBreakpoint() {
        if (currentBreakpoint != null) {
            int oldLineNumber = currentBreakpoint.getLineNumber();
            BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(currentBreakpoint.getPath());
            if (breakpointRenderer != null) {
                breakpointRenderer.setLineActive(oldLineNumber, false);
            }

            currentBreakpoint = null;
        }
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
        final List<Breakpoint> fileBreakpoints = breakpoints.get(file.getLocation().toString());
        final int delta = linesAdded - linesRemoved;

        if (fileBreakpoints != null) {
            LOG.fine("Change in file with breakpoints " + file.getLocation().toString());

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
                breakpointStorage.delete(breakpoint);
                deleteBreakpoint(file, breakpoint);
            }

            for (final Breakpoint breakpoint : toAdd) {
                if (isLineNotEmpty(file, breakpoint.getLineNumber())) {
                    Breakpoint newBreakpoint = new Breakpoint(breakpoint.getType(),
                                                              breakpoint.getLineNumber(),
                                                              breakpoint.getPath(),
                                                              file,
                                                              false);

                    addBreakpoint(newBreakpoint);
                    breakpointStorage.add(newBreakpoint);
                }
            }
        }
    }

    /**
     * Registers events handlers.
     */
    private void registerEventHandlers(EventBus eventBus) {
        eventBus.addHandler(WorkspaceReadyEvent.getType(), new WorkspaceReadyEvent.WorkspaceReadyHandler() {
            @Override
            public void onWorkspaceReady(WorkspaceReadyEvent event) {
                breakpointStorage.readAll().then(breakpoints -> {
                    breakpoints.forEach(BreakpointManagerImpl.this::addBreakpoint);
                });
            }
        });

        eventBus.addHandler(EditorOpenedEvent.TYPE, event -> onOpenEditor(event.getFile().getLocation().toString(), event.getEditor()));
        eventBus.addHandler(FileContentUpdateEvent.TYPE, this::onFileContentUpdate);
        eventBus.addHandler(DeleteProjectEvent.TYPE, event -> {
            if (breakpoints.isEmpty()) {
                return;
            }

            ProjectConfigDto config = event.getProjectConfig();
            String path = config.getPath() + "/";
            deleteBreakpoints(getBreakpointPaths(path));
        });

        eventBus.addHandler(ResourceChangedEvent.getType(), event -> {
            if (event.getDelta().getKind() == ResourceDelta.REMOVED) {
                if (breakpoints.isEmpty()) {
                    return;
                }

                final Resource resource = event.getDelta().getResource();

                Path path = resource.getLocation();

                if (resource.isFolder()) {
                    path.addTrailingSeparator();

                    deleteBreakpoints(getBreakpointPaths(path.toString()));
                } else if (resource.isFile()) {
                    deleteBreakpoints(Collections.singleton(path.toString()));
                }
            }
        });
    }

    /**
     * When source is downloaded then {@link FileContentUpdateEvent} will be generated.
     * After that we have to wait {@link DocumentChangedEvent} to know when {@link TextEditor} will be updated.
     */
    private void onFileContentUpdate(FileContentUpdateEvent event) {
        String filePath = event.getFilePath();
        if (currentBreakpoint != null && currentBreakpoint.getPath().equals(filePath)) {

            EditorPartPresenter editor = getEditorForFile(filePath);
            if (editor instanceof TextEditor) {

                final TextEditor textEditor = (TextEditor)editor;
                textEditor.getDocument()
                          .getDocumentHandle()
                          .getDocEventBus()
                          .addHandler(DocumentChangedEvent.TYPE, docChangedEvent -> {

                              String changedFilePath = docChangedEvent.getDocument().getDocument().getFile().getLocation().toString();
                              if (currentBreakpoint == null || !currentBreakpoint.getPath().equals(changedFilePath)) {
                                  return;
                              }

                              BreakpointRenderer breakpointRenderer = getBreakpointRendererForEditor(editor);
                              if (breakpointRenderer != null) {
                                  breakpointRenderer.setLineActive(currentBreakpoint.getLineNumber(), false);
                                  breakpointRenderer.setLineActive(currentBreakpoint.getLineNumber(), true);

                                  new Timer() {
                                      @Override
                                      public void run() {
                                          textEditor.setCursorPosition(new TextPosition(currentBreakpoint.getLineNumber() + 1, 0));
                                      }
                                  }.schedule(300);
                              }
                          });
            }
        }
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

        breakpointRenderer.addBreakpointMark(lineNumber, this::onLineChange);
        breakpointRenderer.setBreakpointActive(lineNumber, breakpoint.isActive());
    }


    // Debugger events

    @Override
    public void onActiveDebuggerChanged(@Nullable Debugger activeDebugger) {
    }

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
        List<Breakpoint> breakpointsForPath = breakpoints.computeIfAbsent(path, k -> new ArrayList<>());

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
    public void onAllBreakpointsDeleted() { }

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
    public void onValueChanged(List<String> path, String newValue) {
    }

    @Override
    public void addObserver(BreakpointManagerObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(BreakpointManagerObserver observer) {
        observers.remove(observer);
    }
}
