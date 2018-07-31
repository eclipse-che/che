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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.EditorPartStack;

/**
 * Performs closing pane and all opened editors for current pane.
 *
 * @author Vlad Zhukovskiy
 * @author Roman Nikitenko
 */
@Singleton
public class CloseAllAction extends EditorAbstractAction {

  @Inject
  public CloseAllAction(
      EditorAgent editorAgent, EventBus eventBus, CoreLocalizationConstant locale) {
    super(
        locale.editorTabCloseAll(),
        locale.editorTabCloseAllDescription(),
        null,
        editorAgent,
        eventBus);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    EditorPartStack currentPartStack = getEditorPane(event);
    for (EditorPartPresenter editorPart : editorAgent.getOpenedEditorsFor(currentPartStack)) {
      editorAgent.closeEditor(editorPart);
    }
  }
}
