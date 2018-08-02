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
 * Performs closing current pane and all opened editors for this one.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class ClosePaneAction extends EditorAbstractAction {

  @Inject
  public ClosePaneAction(
      EditorAgent editorAgent, EventBus eventBus, CoreLocalizationConstant locale) {
    super(
        locale.editorClosePane(), locale.editorClosePaneDescription(), null, editorAgent, eventBus);
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
