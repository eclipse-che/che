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

import static com.google.gwt.i18n.client.DateTimeFormat.getFormat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Date;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Performs closing all opened editors except selected one for current editor part stack.
 *
 * @author Vlad Zhukovskiy
 * @author Roman Nikitenko
 */
@Singleton
public class CloseOtherAction extends EditorAbstractAction {

  @Inject
  public CloseOtherAction(
      EditorAgent editorAgent, EventBus eventBus, CoreLocalizationConstant locale) {
    super(
        locale.editorTabCloseAllExceptSelected(),
        locale.editorTabCloseAllExceptSelectedDescription(),
        null,
        editorAgent,
        eventBus);
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabled(isFilesToCloseExist(event));
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    EditorPartStack currentPartStack = getEditorPane(event);
    EditorPartPresenter currentEditor = getEditorTab(event).getRelativeEditorPart();
    for (EditorPartPresenter editorPart : editorAgent.getOpenedEditorsFor(currentPartStack)) {
      if (currentEditor != editorPart) {
        editorAgent.closeEditor(editorPart);
        Log.info(
            this.getClass(),
            "actionPerformed() line 62, time: "
                + getFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
      }
    }
  }

  private boolean isFilesToCloseExist(ActionEvent event) {
    EditorPartStack currentPartStack = getEditorPane(event);
    EditorPartPresenter currentEditor = getEditorTab(event).getRelativeEditorPart();
    for (EditorPartPresenter openedEditor : editorAgent.getOpenedEditorsFor(currentPartStack)) {
      if (currentEditor != openedEditor) {
        return true;
      }
    }
    return false;
  }
}
