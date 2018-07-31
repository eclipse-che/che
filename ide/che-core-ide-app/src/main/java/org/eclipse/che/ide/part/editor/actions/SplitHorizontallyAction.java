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

import static org.eclipse.che.ide.api.constraints.Direction.HORIZONTALLY;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * Divides the area of the selected editor on two areas and displays copy horizontally relative to
 * selected editor.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class SplitHorizontallyAction extends EditorAbstractAction {

  @Inject
  public SplitHorizontallyAction(
      EditorAgent editorAgent, EventBus eventBus, CoreLocalizationConstant locale) {
    super(
        locale.editorTabSplitHorizontally(),
        locale.editorTabSplitHorizontallyDescription(),
        null,
        editorAgent,
        eventBus);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    final String tabId = getEditorTab(event).getId();
    final VirtualFile file = getEditorFile(event);
    final Constraints constraints = new Constraints(HORIZONTALLY, tabId);

    editorAgent.openEditor(file, constraints);
  }
}
