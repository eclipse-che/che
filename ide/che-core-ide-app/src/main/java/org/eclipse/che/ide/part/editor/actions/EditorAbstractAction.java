/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.editor.actions;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.web.bindery.event.shared.EventBus;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Base action for editor tab.
 *
 * @author Vlad Zhukovskiy
 */
public abstract class EditorAbstractAction extends AbstractPerspectiveAction {

  public static final String CURRENT_FILE_PROP = "source";
  public static final String CURRENT_TAB_PROP = "tab";
  public static final String CURRENT_PANE_PROP = "pane";

  protected final EventBus eventBus;
  protected final EditorAgent editorAgent;

  public EditorAbstractAction(
      String tooltip,
      String description,
      SVGResource icon,
      EditorAgent editorAgent,
      EventBus eventBus) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), tooltip, description, icon);
    this.eventBus = eventBus;
    this.editorAgent = editorAgent;
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(!editorAgent.getOpenedEditors().isEmpty());
  }

  /**
   * Fetch file from the action event. File should be passed by context menu during construction the
   * last one.
   *
   * @param e action event
   * @return {@link VirtualFile} file.
   * @throws IllegalStateException in case if file not found in action event
   */
  protected VirtualFile getEditorFile(ActionEvent e) {
    Object o = e.getPresentation().getClientProperty(CURRENT_FILE_PROP);

    if (o instanceof VirtualFile) {
      return (VirtualFile) o;
    }

    throw new IllegalStateException("File doesn't provided");
  }

  protected EditorTab getEditorTab(ActionEvent e) {
    Object o = e.getPresentation().getClientProperty(CURRENT_TAB_PROP);

    if (o instanceof EditorTab) {
      return (EditorTab) o;
    }

    throw new IllegalStateException("Tab doesn't provided");
  }

  protected EditorPartStack getEditorPane(ActionEvent e) {
    Object o = e.getPresentation().getClientProperty(CURRENT_PANE_PROP);

    if (o instanceof EditorPartStack) {
      return (EditorPartStack) o;
    }

    throw new IllegalStateException("Editor pane doesn't provided");
  }
}
