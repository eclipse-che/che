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
package org.eclipse.che.ide.part.editor.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * Performs restoring closed editor tab for current editor part stack.
 *
 * @author Vlad Zhukovskiy
 * @author Roman Nikitenko
 */
@Singleton
public class ReopenClosedFileAction extends EditorAbstractAction {

  @Inject
  public ReopenClosedFileAction(
      EventBus eventBus, CoreLocalizationConstant locale, EditorAgent editorAgent) {
    super(
        locale.editorTabReopenClosedTab(),
        locale.editorTabReopenClosedTabDescription(),
        null,
        editorAgent,
        eventBus);
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    EditorPartStack currentPartStack = getEditorPane(event);
    EditorPartPresenter lastClosed = currentPartStack.getLastClosed();

    event.getPresentation().setEnabled(lastClosed != null);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    EditorPartStack currentPartStack = getEditorPane(event);
    EditorPartPresenter lastClosed = currentPartStack.getLastClosed();
    VirtualFile file = lastClosed.getEditorInput().getFile();

    editorAgent.openEditor(file);
  }
}
