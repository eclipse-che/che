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
package org.eclipse.che.ide.debug;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.BreakpointManagerObservable;
import org.eclipse.che.ide.api.debug.BreakpointManagerObserver;
import org.eclipse.che.ide.api.debug.BreakpointRenderer;
import org.eclipse.che.ide.api.debug.BreakpointRenderer.LineChangeAction;
import org.eclipse.che.ide.api.debug.BreakpointStorage;
import org.eclipse.che.ide.api.debug.HasBreakpointRenderer;
import org.eclipse.che.ide.api.debug.HasLocation;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.events.FileContentUpdateEvent;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;

/**
 * Implementation of {@link BreakpointManager} for editor.
 *
 * @author Anatoliy Bazko
 * @author Valeriy Svydenko
 * @author Dmytro Nochevnov
 */
@Singleton
public class BreakpointManagerImpl
    implements BreakpointManager,
        LineChangeAction,
        BreakpointManagerObservable,
        DebuggerManagerObserver {

  private static final Logger LOG = Logger.getLogger(BreakpointManagerImpl.class.getName());

  private final EditorAgent editorAgent;
  private final DebuggerManager debuggerManager;
  private final BreakpointStorage breakpointStorage;
  private final List<BreakpointManagerObserver> observers;
  private final Set<Breakpoint> activeBreakpoints;

  private Location suspendedLocation;

  @Inject
  public BreakpointManagerImpl(
      EditorAgent editorAgent,
      DebuggerManager debuggerManager,
      EventBus eventBus,
      BreakpointStorage breakpointStorage) {
    this.editorAgent = editorAgent;
    this.breakpointStorage = breakpointStorage;
    this.debuggerManager = debuggerManager;
    this.observers = new ArrayList<>();
    this.debuggerManager.addObserver(this);
    this.activeBreakpoints = new HashSet<>();

    registerEventHandlers(eventBus);
  }

  @Override
  public void changeBreakpointState(final int lineNumber) {
    EditorPartPresenter editor = editorAgent.getActiveEditor();
    if (editor == null) {
      return;
    }

    final VirtualFile activeFile = editor.getEditorInput().getFile();
    Optional<Breakpoint> existedBreakpoint =
        breakpointStorage.get(activeFile.getLocation().toString(), lineNumber + 1);

    if (existedBreakpoint.isPresent()) {
      delete(existedBreakpoint.get());
    } else {
      if (activeFile instanceof HasLocation) {
        addBreakpoint(
            activeFile, new BreakpointImpl(((HasLocation) activeFile).toLocation(lineNumber + 1)));
      } else {
        LOG.warning("Impossible to figure debug location for: " + activeFile.getLocation());
        return;
      }
    }
  }

  @Override
  public boolean isActive(Breakpoint breakpoint) {
    return activeBreakpoints.contains(breakpoint);
  }

  private void addBreakpoint(final VirtualFile file, final Breakpoint breakpoint) {
    if (isLineEmpty(file, breakpoint.getLocation().getLineNumber())) {
      return;
    }
    breakpointStorage.add(breakpoint);

    final BreakpointRenderer renderer = getBreakpointRendererForFile(file.getLocation().toString());

    if (renderer != null) {
      renderer.setBreakpointMark(breakpoint, false, BreakpointManagerImpl.this::onLineChange);
    }

    for (BreakpointManagerObserver observer : observers) {
      observer.onBreakpointAdded(breakpoint);
    }

    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      debugger.addBreakpoint(breakpoint);
    }
  }

  /** Indicates if line of code to add breakpoint at is executable. */
  private boolean isLineEmpty(final VirtualFile activeFile, int lineNumber) {
    EditorPartPresenter editor = getEditorForFile(activeFile.getLocation().toString());
    if (editor instanceof TextEditor) {
      Document document = ((TextEditor) editor).getDocument();
      return document.getLineContent(lineNumber - 1).trim().isEmpty();
    }

    return false;
  }

  @Override
  public List<Breakpoint> getAll() {
    return breakpointStorage.getAll();
  }

  private void setSuspendedLocation(Location location) {
    deleteSuspendedLocation();
    suspendedLocation = location;

    EditorPartPresenter editor = getEditorForFile(location.getTarget());
    if (editor != null) {
      VirtualFile activeFile = editor.getEditorInput().getFile();

      BreakpointRenderer renderer =
          getBreakpointRendererForFile(activeFile.getLocation().toString());
      if (renderer != null) {
        renderer.setLineActive(location.getLineNumber() - 1, true);
      }
    }
  }

  @Override
  public void deleteAll() {
    breakpointStorage
        .getAll()
        .forEach(
            breakpoint -> {
              BreakpointRenderer renderer =
                  getBreakpointRendererForFile(breakpoint.getLocation().getTarget());
              if (renderer != null) {
                renderer.removeBreakpointMark(breakpoint.getLocation().getLineNumber() - 1);
              }
            });

    breakpointStorage.clear();
    activeBreakpoints.clear();

    for (BreakpointManagerObserver observer : observers) {
      observer.onAllBreakpointsDeleted();
    }

    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      debugger.deleteAllBreakpoints();
    }
  }

  @Override
  public void update(final Breakpoint breakpoint) {
    breakpointStorage.update(breakpoint);
    activeBreakpoints.remove(breakpoint);

    BreakpointRenderer renderer =
        getBreakpointRendererForFile(breakpoint.getLocation().getTarget());
    if (renderer != null) {
      renderer.setBreakpointMark(breakpoint, false, BreakpointManagerImpl.this::onLineChange);
    }

    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      debugger
          .deleteBreakpoint(breakpoint)
          .then(
              success -> {
                if (breakpoint.isEnabled()) {
                  debugger.addBreakpoint(breakpoint);
                }
              });
    }

    for (BreakpointManagerObserver observer : observers) {
      observer.onBreakpointUpdated(breakpoint);
    }
  }

  @Override
  public void delete(Breakpoint breakpoint) {
    breakpointStorage.delete(breakpoint);
    activeBreakpoints.remove(breakpoint);

    BreakpointRenderer renderer =
        getBreakpointRendererForFile(breakpoint.getLocation().getTarget());
    if (renderer != null) {
      renderer.removeBreakpointMark(breakpoint.getLocation().getLineNumber() - 1);
    }

    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      debugger.deleteBreakpoint(breakpoint);
    }

    for (BreakpointManagerObserver observer : observers) {
      observer.onBreakpointDeleted(breakpoint);
    }
  }

  private void deleteBreakpoints(String parentPath) {
    List<Breakpoint> breakpoints2delete =
        breakpointStorage
            .getAll()
            .stream()
            .filter(breakpoint -> breakpoint.getLocation().getTarget().startsWith(parentPath))
            .collect(Collectors.toList());

    for (Breakpoint breakpoint : breakpoints2delete) {
      breakpointStorage.delete(breakpoint);
      activeBreakpoints.remove(breakpoint);

      BreakpointRenderer renderer =
          getBreakpointRendererForFile(breakpoint.getLocation().getTarget());
      if (renderer != null) {
        renderer.removeBreakpointMark(breakpoint.getLocation().getLineNumber() - 1);
      }

      for (BreakpointManagerObserver observer : observers) {
        observer.onBreakpointDeleted(breakpoint);
      }
    }
  }

  private void deleteSuspendedLocation() {
    if (suspendedLocation != null) {
      BreakpointRenderer renderer = getBreakpointRendererForFile(suspendedLocation.getTarget());
      if (renderer != null) {
        renderer.setLineActive(suspendedLocation.getLineNumber() - 1, false);
      }

      suspendedLocation = null;
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
      final BreakpointRenderer renderer = ((HasBreakpointRenderer) editor).getBreakpointRenderer();
      if (renderer != null && renderer.isReady()) {
        return renderer;
      }
    }

    return null;
  }

  @Override
  public void onLineChange(
      final VirtualFile file, final int firstLine, final int linesAdded, final int linesRemoved) {

    final List<Breakpoint> fileBreakpoints =
        breakpointStorage.getByPath(file.getLocation().toString());
    final int delta = linesAdded - linesRemoved;

    if (fileBreakpoints != null) {
      LOG.fine("Change in file with breakpoints " + file.getLocation().toString());

      final List<Breakpoint> toRemove = new ArrayList<>();
      final List<Breakpoint> toAdd = new ArrayList<>();

      for (final Breakpoint breakpoint : fileBreakpoints) {
        final int lineNumber = breakpoint.getLocation().getLineNumber() - 1;

        // line removed
        if (firstLine <= lineNumber && lineNumber < firstLine + linesRemoved) {
          toRemove.add(breakpoint);
          continue;
        }

        // line modified
        if (firstLine <= lineNumber && lineNumber < firstLine + linesAdded) {
          toRemove.add(breakpoint);
          continue;
        }

        // line shifted
        if (lineNumber >= firstLine + Math.abs(delta)) {
          Location currentLocation = breakpoint.getLocation();
          Location newLocation =
              new LocationImpl(
                  currentLocation.getTarget(),
                  currentLocation.getLineNumber() + delta,
                  currentLocation.isExternalResource(),
                  currentLocation.getExternalResourceId(),
                  currentLocation.getResourceProjectPath(),
                  currentLocation.getMethod(),
                  currentLocation.getThreadId());

          toRemove.add(breakpoint);
          toAdd.add(
              new BreakpointImpl(
                  newLocation, breakpoint.isEnabled(), breakpoint.getBreakpointConfiguration()));
        }
      }

      for (final Breakpoint breakpoint : toRemove) {
        delete(breakpoint);
      }

      for (final Breakpoint breakpoint : toAdd) {
        addBreakpoint(file, breakpoint);
      }
    }
  }

  /** Registers events handlers. */
  private void registerEventHandlers(EventBus eventBus) {
    eventBus.addHandler(
        EditorOpenedEvent.TYPE,
        event -> onOpenEditor(event.getFile().getLocation().toString(), event.getEditor()));

    eventBus.addHandler(FileContentUpdateEvent.TYPE, this::onFileContentUpdate);

    eventBus.addHandler(
        ResourceChangedEvent.getType(),
        event -> {
          if (event.getDelta().getKind() == ResourceDelta.REMOVED) {
            final Resource resource = event.getDelta().getResource();

            Path path = resource.getLocation();
            if (resource.isFolder()) {
              deleteBreakpoints(path.addTrailingSeparator().toString());
            } else if (resource.isFile()) {
              deleteBreakpoints(path.toString());
            }
          }
        });
  }

  /**
   * When source is downloaded then {@link FileContentUpdateEvent} will be generated. After that we
   * have to wait {@link DocumentChangedEvent} to know when {@link TextEditor} will be updated.
   */
  private void onFileContentUpdate(FileContentUpdateEvent event) {
    String filePath = event.getFilePath();
    if (suspendedLocation != null && suspendedLocation.getTarget().equals(filePath)) {

      EditorPartPresenter editor = getEditorForFile(filePath);
      if (editor instanceof TextEditor) {

        final TextEditor textEditor = (TextEditor) editor;
        textEditor
            .getDocument()
            .getDocumentHandle()
            .getDocEventBus()
            .addHandler(
                DocumentChangedEvent.TYPE,
                docChangedEvent -> {
                  String changedFilePath =
                      docChangedEvent
                          .getDocument()
                          .getDocument()
                          .getFile()
                          .getLocation()
                          .toString();
                  if (suspendedLocation == null
                      || !suspendedLocation.getTarget().equals(changedFilePath)) {
                    return;
                  }

                  BreakpointRenderer breakpointRenderer = getBreakpointRendererForEditor(editor);
                  if (breakpointRenderer != null) {
                    new Timer() {
                      @Override
                      public void run() {
                        breakpointRenderer.setLineActive(
                            suspendedLocation.getLineNumber() - 1, true);
                        textEditor.setCursorPosition(
                            new TextPosition(suspendedLocation.getLineNumber(), 0));
                      }
                    }.schedule(300);
                  }
                });
      }
    }
  }

  /** The new file has been opened in the editor. Method reads breakpoints. */
  private void onOpenEditor(String filePath, EditorPartPresenter editor) {
    final BreakpointRenderer renderer = getBreakpointRendererForEditor(editor);
    if (renderer != null) {
      breakpointStorage
          .getByPath(filePath)
          .forEach(
              breakpoint ->
                  renderer.setBreakpointMark(
                      breakpoint,
                      activeBreakpoints.contains(breakpoint),
                      BreakpointManagerImpl.this::onLineChange));

      if (suspendedLocation != null && suspendedLocation.getTarget().equals(filePath)) {
        renderer.setLineActive(suspendedLocation.getLineNumber() - 1, true);
      }
    }
  }

  // Debugger events

  @Override
  public void onActiveDebuggerChanged(@Nullable Debugger activeDebugger) {}

  @Override
  public void onDebuggerAttached(DebuggerDescriptor debuggerDescriptor) {}

  @Override
  public void onDebuggerDisconnected() {
    breakpointStorage
        .getAll()
        .forEach(
            breakpoint -> {
              BreakpointRenderer renderer =
                  getBreakpointRendererForFile(breakpoint.getLocation().getTarget());
              if (renderer != null) {
                renderer.setBreakpointMark(
                    breakpoint, false, BreakpointManagerImpl.this::onLineChange);
              }
            });

    deleteSuspendedLocation();

    activeBreakpoints.clear();
    for (BreakpointManagerObserver observer : observers) {
      observer.onBreakpointUpdated(null);
    }
  }

  @Override
  public void onBreakpointAdded(Breakpoint breakpoint) {}

  @Override
  public void onBreakpointActivated(String filePath, int lineNumber) {
    breakpointStorage
        .get(filePath, lineNumber)
        .ifPresent(
            breakpoint -> {
              BreakpointRenderer renderer =
                  getBreakpointRendererForFile(breakpoint.getLocation().getTarget());
              if (renderer != null) {
                renderer.setBreakpointMark(
                    breakpoint, true, BreakpointManagerImpl.this::onLineChange);
              }

              activeBreakpoints.add(breakpoint);
              for (BreakpointManagerObserver observer : observers) {
                observer.onBreakpointUpdated(breakpoint);
              }
            });
  }

  @Override
  public void onBreakpointDeleted(Breakpoint breakpoint) {}

  @Override
  public void onAllBreakpointsDeleted() {}

  @Override
  public void onPreStepInto() {
    deleteSuspendedLocation();
  }

  @Override
  public void onPreStepOut() {
    deleteSuspendedLocation();
  }

  @Override
  public void onPreStepOver() {
    deleteSuspendedLocation();
  }

  @Override
  public void onPreResume() {
    deleteSuspendedLocation();
  }

  @Override
  public void onBreakpointStopped(String filePath, Location location) {
    setSuspendedLocation(
        new LocationImpl(filePath, location.getLineNumber(), location.getResourceProjectPath()));
  }

  @Override
  public void onValueChanged(Variable variable, long threadId, int frameIndex) {}

  @Override
  public void addObserver(BreakpointManagerObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(BreakpointManagerObserver observer) {
    observers.remove(observer);
  }
}
