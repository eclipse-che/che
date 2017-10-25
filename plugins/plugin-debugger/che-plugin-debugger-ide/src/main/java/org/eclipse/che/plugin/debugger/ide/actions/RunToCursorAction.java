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
package org.eclipse.che.plugin.debugger.ide.actions;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.debug.DebuggerManagerObserver;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

/**
 * Action which allows run application to cursor in debugger session
 *
 * @author Igor Vinokur
 */
public class RunToCursorAction extends AbstractPerspectiveAction
    implements DebuggerManagerObserver {

  private final DebuggerManager debuggerManager;
  private final EditorAgent editorAgent;
  private Breakpoint breakpoint;

  @Inject
  public RunToCursorAction(
      DebuggerManager debuggerManager,
      EditorAgent editorAgent,
      DebuggerLocalizationConstant locale,
      DebuggerResources resources) {
    super(
        Collections.singletonList(PROJECT_PERSPECTIVE_ID),
        locale.runToCursor(),
        locale.runToCursorDescription(),
        null,
        resources.runToCursor());
    this.debuggerManager = debuggerManager;
    this.debuggerManager.addObserver(this);
    this.editorAgent = editorAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      EditorPartPresenter editor = editorAgent.getActiveEditor();
      int line = ((TextEditor) editor).getCursorPosition().getLine() + 1;
      String target = editor.getEditorInput().getFile().getLocation().toString();
      breakpoint = new BreakpointImpl(new LocationImpl(target, line));
      debugger.addBreakpoint(breakpoint);
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {}

  @Override
  public void onBreakpointAdded(Breakpoint breakpoint) {
    if (breakpoint.equals(this.breakpoint)) {
      Debugger debugger = debuggerManager.getActiveDebugger();
      debugger.resume();
    }
  }

  @Override
  public void onBreakpointStopped(String filePath, Location location) {
    if (breakpoint != null) {
      Debugger debugger = debuggerManager.getActiveDebugger();
      debugger.deleteBreakpoint(breakpoint);
      breakpoint = null;
    }
  }

  @Override
  public void onDebuggerAttached(DebuggerDescriptor debuggerDescriptor, Promise<Void> connect) {}

  @Override
  public void onDebuggerDisconnected() {}

  @Override
  public void onBreakpointActivated(String filePath, int lineNumber) {}

  @Override
  public void onBreakpointDeleted(Breakpoint breakpoint) {}

  @Override
  public void onAllBreakpointsDeleted() {}

  @Override
  public void onPreStepInto() {}

  @Override
  public void onPreStepOut() {}

  @Override
  public void onPreStepOver() {}

  @Override
  public void onPreResume() {}

  @Override
  public void onValueChanged(Variable variable, long threadId, int frameIndex) {}

  @Override
  public void onActiveDebuggerChanged(Debugger activeDebugger) {}
}
