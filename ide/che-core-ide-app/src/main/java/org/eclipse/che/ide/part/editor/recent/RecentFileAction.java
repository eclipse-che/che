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
package org.eclipse.che.ide.part.editor.recent;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.editor.recent.RecentFileStore.getShortPath;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.resources.File;

/**
 * Action for the recent file. When user click on this action, recent file opens again in editor.
 *
 * @author Vlad Zhukovskiy
 */
public class RecentFileAction extends AbstractPerspectiveAction {

  private final File file;
  private final EditorAgent editorAgent;

  @Inject
  public RecentFileAction(@Assisted File file, EditorAgent editorAgent) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), getShortPath(file.getLocation().toString()));
    this.file = file;
    this.editorAgent = editorAgent;
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    editorAgent.openEditor(file);
  }

  /**
   * Return an id for the registration in action manager. ID value forms based on file path to
   * define unique key for this action.
   *
   * @return action id
   */
  public String getId() {
    return "recent/" + file.getLocation();
  }
}
