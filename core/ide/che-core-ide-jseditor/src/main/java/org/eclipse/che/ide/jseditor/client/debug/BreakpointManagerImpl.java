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
package org.eclipse.che.ide.jseditor.client.debug;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorOpenedEventHandler;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.project.node.HasProjectConfig.ProjectConfig;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.api.project.tree.VirtualFileImpl;
import org.eclipse.che.ide.api.project.tree.VirtualFileInfo;
import org.eclipse.che.ide.debug.Breakpoint;
import org.eclipse.che.ide.debug.Breakpoint.Type;
import org.eclipse.che.ide.debug.BreakpointManager;
import org.eclipse.che.ide.debug.BreakpointRenderer;
import org.eclipse.che.ide.debug.BreakpointRenderer.LineChangeAction;
import org.eclipse.che.ide.debug.BreakpointStateEvent;
import org.eclipse.che.ide.debug.BreakpointStateEventHandler;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerStateEvent;
import org.eclipse.che.ide.debug.DebuggerStateEventHandler;
import org.eclipse.che.ide.debug.HasBreakpointRenderer;
import org.eclipse.che.ide.debug.dto.BreakpointDto;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static org.eclipse.che.ide.debug.DebuggerStateEvent.DebuggerState.CONNECTED;
import static org.eclipse.che.ide.debug.DebuggerStateEvent.DebuggerState.DISCONNECTED;

/**
 * Implementation of {@link BreakpointManager} for jseditor.
 *
 * @author Anatoliy Bazko
 * @author Valeriy Svydenko
 */
public class BreakpointManagerImpl implements BreakpointManager, LineChangeAction {

    /**
     * The logger.
     */
    private static final Logger LOG                           = Logger.getLogger(BreakpointManagerImpl.class.getName());
    private static final String LOCAL_STORAGE_BREAKPOINTS_KEY = "che-breakpoints";

    private final Map<String, List<Breakpoint>> breakpoints;
    private final EditorAgent                   editorAgent;
    private final DebuggerManager               debuggerManager;
    private final DtoFactory                    dtoFactory;


    private Breakpoint                       currentBreakpoint;
    private DebuggerStateEvent.DebuggerState debuggerState;

    @Inject
    public BreakpointManagerImpl(final EditorAgent editorAgent,
                                 final DebuggerManager debuggerManager,
                                 final EventBus eventBus,
                                 final DtoFactory dtoFactory) {
        this.editorAgent = editorAgent;
        this.breakpoints = new HashMap<>();
        this.debuggerManager = debuggerManager;
        this.debuggerState = DISCONNECTED;
        this.dtoFactory = dtoFactory;

        registerEventHandlers(eventBus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeBreakpointState(final int lineNumber) {
        final Debugger debugger = debuggerManager.getDebugger();
        if (debugger == null) {
            return;
        }

        EditorPartPresenter editor = editorAgent.getActiveEditor();
        if (editor == null) {
            return;
        }

        final VirtualFile activeFile = editor.getEditorInput().getFile();

        List<Breakpoint> breakpointsForPath = this.breakpoints.get(activeFile.getPath());
        if (breakpointsForPath != null) {
            for (final Breakpoint breakpoint : breakpointsForPath) {
                if (breakpoint.getLineNumber() == lineNumber) {
                    // breakpoint already exists at given line
                    deleteBreakpoint(activeFile, debugger, breakpoint);
                    return;
                }
            }
        }

        if (isCodeExecutable(activeFile, lineNumber)) {
            addBreakpoint(activeFile, debugger, lineNumber);
        }
    }

    /**
     * Deletes breakpoint from the list and JVM.
     * Removes breakpoint mark.
     */
    private void deleteBreakpoint(final VirtualFile activeFile,
                                  final Debugger debugger,
                                  final Breakpoint breakpoint) {
        doDeleteBreakpoint(activeFile, breakpoint);

        debugger.deleteBreakpoint(activeFile, breakpoint.getLineNumber(), new AsyncCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
            }

            @Override
            public void onFailure(final Throwable exception) {
                if (debuggerState == CONNECTED) {
                    doAddBreakpoint(activeFile, breakpoint);
                }
            }
        });
    }

    /**
     * Deletes breakpoint from the list.
     */
    private void doDeleteBreakpoint(VirtualFile activeFile, Breakpoint breakpoint) {
        BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(activeFile);
        if (breakpointRenderer != null) {
            breakpointRenderer.removeBreakpointMark(breakpoint.getLineNumber());
        }

        String path = breakpoint.getPath();

        List<Breakpoint> fileBreakpoints = breakpoints.get(path);
        if (fileBreakpoints != null) {
            fileBreakpoints.remove(breakpoint);
            if (fileBreakpoints.isEmpty()) {
                breakpoints.remove(activeFile.getPath());
            }
        }

        preserveBreakpoints();
    }

    /**
     * Adds breakpoint to the list and JVM.
     */
    private void addBreakpoint(final VirtualFile activeFile,
                               final Debugger debugger,
                               final int lineNumber) {
        debugger.addBreakpoint(activeFile, lineNumber, new AsyncCallback<Breakpoint>() {
            @Override
            public void onSuccess(final Breakpoint result) {
                Breakpoint activeBreakpoint = new Breakpoint(result.getType(),
                                                             result.getLineNumber(),
                                                             result.getPath(),
                                                             result.getFile(),
                                                             true);
                doAddBreakpoint(activeFile, activeBreakpoint);
            }

            @Override
            public void onFailure(final Throwable exception) {
                Breakpoint inactiveBreakpoint = new Breakpoint(Breakpoint.Type.BREAKPOINT,
                                                               lineNumber,
                                                               activeFile.getPath(),
                                                               activeFile,
                                                               false);
                doAddBreakpoint(activeFile, inactiveBreakpoint);
            }
        });
    }

    private void doAddBreakpoint(VirtualFile activeFile, Breakpoint breakpoint) {
        String path = breakpoint.getPath();

        List<Breakpoint> fileBreakpoints = breakpoints.get(path);
        if (fileBreakpoints == null) {
            fileBreakpoints = new ArrayList<Breakpoint>();
            breakpoints.put(path, fileBreakpoints);
        }

        fileBreakpoints.add(breakpoint);

        final BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(activeFile);
        if (breakpointRenderer != null) {
            breakpointRenderer.addBreakpointMark(breakpoint.getLineNumber(), new LineChangeAction() {
                @Override
                public void onLineChange(VirtualFile file, int firstLine, int linesAdded, int linesRemoved) {
                    BreakpointManagerImpl.this.onLineChange(file, firstLine, linesAdded, linesRemoved);
                }
            });
            breakpointRenderer.setBreakpointActive(breakpoint.getLineNumber(), breakpoint.isActive());
        }

        preserveBreakpoints();
    }

    /**
     * Indicates if line of code to add breakpoint at is executable.
     */
    private boolean isCodeExecutable(final VirtualFile activeFile, int lineNumber) {
        EditorPartPresenter editor = getEditorForFile(activeFile);
        if (editor instanceof EmbeddedTextEditorPresenter) {
            Document document = ((EmbeddedTextEditorPresenter)editor).getDocument();
            return !document.getLineContent(lineNumber).trim().isEmpty();
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllBreakpoints() {
        LOG.fine("Remove all breakpoints");
        for (final Entry<String, List<Breakpoint>> entry : this.breakpoints.entrySet()) {
            final String path = entry.getKey();
            final List<Breakpoint> pathBreakpoints = entry.getValue();

            removeBreakpointsForPath(path, pathBreakpoints);
        }
        breakpoints.clear();
        preserveBreakpoints();
    }

    private void removeBreakpointsForPath(final String path, final List<Breakpoint> pathBreakpoints) {
        LOG.fine("\tRemove all breakpoints for path " + path);
        for (final Breakpoint breakpoint : pathBreakpoints) {
            BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getFile());
            if (breakpointRenderer != null) {
                breakpointRenderer.removeBreakpointMark(breakpoint.getLineNumber());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Breakpoint> getBreakpointList() {
        final List<Breakpoint> result = new ArrayList<>();
        for (final List<Breakpoint> fileBreakpoints : breakpoints.values()) {
            result.addAll(fileBreakpoints);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentBreakpoint(int lineNumber) {
        LOG.fine("Mark current breakpoint on line " + lineNumber);
        removeCurrentBreakpoint();

        EditorPartPresenter editor = editorAgent.getActiveEditor();
        if (editor != null) {
            VirtualFile activeFile = editor.getEditorInput().getFile();
            doSetCurrentBreakpoint(activeFile, lineNumber);
        }

    }

    private void doSetCurrentBreakpoint(VirtualFile activeFile, int lineNumber) {
        currentBreakpoint = new Breakpoint(Type.CURRENT, lineNumber, activeFile.getPath(), activeFile, true);

        BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(activeFile);
        if (breakpointRenderer != null) {
            breakpointRenderer.setLineActive(lineNumber, true);
        }

        preserveBreakpoints();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Breakpoint getCurrentBreakpoint() {
        return currentBreakpoint;
    }

    public void removeCurrentBreakpoint() {
        if (currentBreakpoint != null) {
            int oldLineNumber = currentBreakpoint.getLineNumber();
            LOG.fine("Unmark current breakpoint on line " + oldLineNumber);
            BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(currentBreakpoint.getFile());
            if (breakpointRenderer != null) {
                breakpointRenderer.setLineActive(oldLineNumber, false);
            }

            currentBreakpoint = null;
        }

        preserveBreakpoints();
    }

    @Override
    public boolean isCurrentBreakpoint(int lineNumber) {
        if (this.currentBreakpoint != null) {
            EditorPartPresenter editor = editorAgent.getActiveEditor();
            if (editor != null) {
                VirtualFile activeFile = editor.getEditorInput().getFile();
                return activeFile.getPath().equals(currentBreakpoint.getPath()) && lineNumber == currentBreakpoint.getLineNumber();
            }
        }
        return false;
    }

    @Nullable
    private EditorPartPresenter getEditorForFile(VirtualFile fileNode) {
        final Map<String, EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();
        for (final String key : openedEditors.keySet()) {
            final EditorPartPresenter editor = openedEditors.get(key);
            if (fileNode.getPath().equals(editor.getEditorInput().getFile().getPath())) {
                return editor;
            }
        }
        return null;
    }

    @Nullable
    private BreakpointRenderer getBreakpointRendererForFile(VirtualFile fileNode) {
        final EditorPartPresenter editor = getEditorForFile(fileNode);
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
                                         breakpoint.getMessage(),
                                         breakpoint.isActive()));
            }

            Debugger debugger = debuggerManager.getDebugger();
            if (debugger != null) {
                for (final Breakpoint breakpoint : toRemove) {
                    deleteBreakpoint(file, debugger, breakpoint);
                }
                for (final Breakpoint breakpoint : toAdd) {
                    if (isCodeExecutable(file, breakpoint.getLineNumber())) {
                        addBreakpoint(file, debugger, breakpoint.getLineNumber());
                    }
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
                onOpenEditor(event.getFile(), event.getEditor());
            }
        });

        eventBus.addHandler(DebuggerStateEvent.TYPE, new DebuggerStateEventHandler() {
            @Override
            public void onStateChanged(DebuggerStateEvent event) {
                BreakpointManagerImpl.this.debuggerState = event.getState();
                if (event.isConnectedState()) {
                    onDebuggerConnected(event.getDebugger());
                } else if (event.isDisconnectedState()) {
                    onDebuggerDisconnected();
                } else if (event.isInitializedState()) {
                    onDebugInitialized(event.getDebugger());
                }
            }
        });

        eventBus.addHandler(BreakpointStateEvent.TYPE, new BreakpointStateEventHandler() {
            @Override
            public void onStateChanged(BreakpointStateEvent event) {
                onBreakpointStateChanged(event.getState(), event.getFilePath(), event.getLineNumber());
            }
        });
    }

    /**
     * VM has changed breakpoint state. Sets respective mark.
     */
    private void onBreakpointStateChanged(BreakpointStateEvent.BreakpointState state, String filePath, int lineNumber) {
        if (state == BreakpointStateEvent.BreakpointState.ACTIVE) {
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
                                                                    breakpoint.getMessage(),
                                                                    true);
                    breakpointsForPath.set(i, newActiveBreakpoint);

                    BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getFile());
                    if (breakpointRenderer != null) {
                        breakpointRenderer.setBreakpointActive(breakpoint.getLineNumber(), true);
                    }
                }
            }
        }
    }

    /**
     * Debugger has been initialized.
     * Method adds stored breakpoints.
     */
    private void onDebugInitialized(Debugger debugger) {
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
                                                             .setMediaType(dto.getFileMediaType())
                                                             .setProject(new ProjectConfig(dto.getFileProjectConfig()))
                                                             .build();

            VirtualFile file = new VirtualFileImpl(virtualFileInfo);

            if (dto.getType() == Type.CURRENT) {
                doSetCurrentBreakpoint(file, dto.getLineNumber());
            } else {
                addBreakpoint(file, debugger, dto.getLineNumber());
            }
        }
    }

    /**
     * Debugger has disconnected from the JVM.
     * Method marks breakpoint as inactive.
     */
    private void onDebuggerDisconnected() {
        for (Entry<String, List<Breakpoint>> entry : breakpoints.entrySet()) {
            List<Breakpoint> breakpointsForPath = entry.getValue();

            for (int i = 0; i < breakpointsForPath.size(); i++) {
                Breakpoint breakpoint = breakpointsForPath.get(i);

                if (breakpoint.isActive()) {
                    Breakpoint newInactiveBreakpoint = new Breakpoint(breakpoint.getType(),
                                                                      breakpoint.getLineNumber(),
                                                                      breakpoint.getPath(),
                                                                      breakpoint.getFile(),
                                                                      breakpoint.getMessage(),
                                                                      false);
                    breakpointsForPath.set(i, newInactiveBreakpoint);

                    BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getFile());
                    if (breakpointRenderer != null) {
                        breakpointRenderer.setBreakpointActive(breakpoint.getLineNumber(), false);
                    }
                }
            }
        }

        removeCurrentBreakpoint();
    }

    /**
     * Debugger has connected to the JVM.
     * Method adds breakpoints and marks them as active.
     */
    private void onDebuggerConnected(Debugger debugger) {
        for (Entry<String, List<Breakpoint>> entry : breakpoints.entrySet()) {
            final List<Breakpoint> breakpointsForPath = entry.getValue();

            for (int i = 0; i < breakpointsForPath.size(); i++) {
                final Breakpoint breakpoint = breakpointsForPath.get(i);
                final int breakpointNumber = i;

                debugger.addBreakpoint(breakpoint.getFile(), breakpoint.getLineNumber(), new AsyncCallback<Breakpoint>() {
                    @Override
                    public void onSuccess(final Breakpoint result) {
                        Breakpoint newActiveBreakpoint = new Breakpoint(breakpoint.getType(),
                                                                        breakpoint.getLineNumber(),
                                                                        breakpoint.getPath(),
                                                                        breakpoint.getFile(),
                                                                        breakpoint.getMessage(),
                                                                        true);
                        breakpointsForPath.set(breakpointNumber, newActiveBreakpoint);

                        BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(breakpoint.getFile());
                        if (breakpointRenderer != null) {
                            breakpointRenderer.setBreakpointActive(breakpoint.getLineNumber(), true);
                        }
                    }

                    @Override
                    public void onFailure(final Throwable exception) {
                    }
                });
            }
        }
    }

    /**
     * The new file has been opened in the editor.
     * Method readds breakpoints.
     */
    private void onOpenEditor(VirtualFile file, EditorPartPresenter editor) {
        final List<Breakpoint> fileBreakpoints = breakpoints.get(file.getPath());

        if (fileBreakpoints != null) {
            final BreakpointRenderer breakpointRenderer = getBreakpointRendererForEditor(editor);

            if (breakpointRenderer != null) {
                for (final Breakpoint breakpoint : fileBreakpoints) {
                    reAddBreakpointMark(breakpointRenderer, breakpoint);
                }
            }
        }

        if (currentBreakpoint != null && file.getPath().equals(currentBreakpoint.getPath())) {
            BreakpointRenderer breakpointRenderer = getBreakpointRendererForFile(file);
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
                dto.setMessage(breakpoint.getMessage());
                dto.setPath(breakpoint.getPath());
                dto.setLineNumber(breakpoint.getLineNumber());
                dto.setFileMediaType(breakpoint.getFile().getMediaType());
                dto.setFileProjectConfig(breakpoint.getFile().getProject().getProjectConfig());

                allDtoBreakpoints.add(dto);
            }

            String data = dtoFactory.toJson(allDtoBreakpoints);
            localStorage.setItem(LOCAL_STORAGE_BREAKPOINTS_KEY, data);
        }
    }

}