/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.actions;

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.debug.HasLocation;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;

/**
 * Action which allows run application to cursor in debugger session
 *
 * @author Igor Vinokur
 */
public class RunToCursorAction extends AbstractPerspectiveAction {

  private final DebuggerManager debuggerManager;
  private final EditorAgent editorAgent;

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
        resources.runToCursor());
    this.debuggerManager = debuggerManager;
    this.editorAgent = editorAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    if (debugger != null) {
      EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
      if (activeEditor instanceof TextEditor) {
        int lineNumber = ((TextEditor) activeEditor).getCursorPosition().getLine() + 1;

        VirtualFile file = activeEditor.getEditorInput().getFile();
        if (file instanceof HasLocation) {
          Location location = ((HasLocation) file).toLocation(lineNumber);
          debugger.runToLocation(location);
        }
      }
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    Debugger debugger = debuggerManager.getActiveDebugger();
    event.getPresentation().setEnabled(debugger != null && debugger.isSuspended());
  }
}
