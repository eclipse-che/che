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
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.part.editor.event.CloseNonPinnedEditorsEvent;

/**
 * Performs closing editor tabs with unpinned status for current editor part stack.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CloseAllExceptPinnedAction extends EditorAbstractAction {

  @Inject
  public CloseAllExceptPinnedAction(
      EditorAgent editorAgent, EventBus eventBus, CoreLocalizationConstant locale) {
    super(
        locale.editorTabCloseAllButPinned(),
        locale.editorTabCloseAllButPinnedDescription(),
        null,
        editorAgent,
        eventBus);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    eventBus.fireEvent(new CloseNonPinnedEditorsEvent(getEditorTab(e)));
  }
}
