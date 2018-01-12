/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;

/**
 * Opens file based resources taken from the application context in the editor.
 *
 * @author Vitaliy Guliy
 * @author Vlad Zhukovskyi
 * @see AppContext#getResources()
 */
@Singleton
public class EditFileAction extends AbstractPerspectiveAction {

  private final AppContext appContext;
  private final EditorAgent editorAgent;

  @Inject
  public EditFileAction(AppContext appContext, Resources resources, EditorAgent editorAgent) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), "Edit file", null, resources.defaultFile());
    this.appContext = appContext;
    this.editorAgent = editorAgent;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    final Resource[] resources = appContext.getResources();

    checkState(
        resources != null && resources.length == 1 && resources[0] instanceof File,
        "Files only are allowed to be opened in editor");

    editorAgent.openEditor((File) resources[0]);
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent e) {
    final Resource[] resources = appContext.getResources();

    e.getPresentation().setVisible(true);
    e.getPresentation()
        .setEnabled(resources != null && resources.length == 1 && resources[0] instanceof File);
  }
}
