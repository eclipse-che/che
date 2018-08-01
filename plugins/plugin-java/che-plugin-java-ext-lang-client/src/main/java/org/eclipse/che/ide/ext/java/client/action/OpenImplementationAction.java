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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.navigation.openimplementation.OpenImplementationPresenter;

/**
 * Action which is opened the implementations of selected JAva Element.
 *
 * @author Valeriy Svydenko
 */
public class OpenImplementationAction extends JavaEditorAction {

  private final OpenImplementationPresenter openImplementationPresenter;

  @Inject
  public OpenImplementationAction(
      JavaLocalizationConstant constant,
      EditorAgent editorAgent,
      OpenImplementationPresenter openImplementationPresenter,
      FileTypeRegistry fileTypeRegistry) {
    super(
        constant.openImplementationActionName(),
        constant.openImplementationDescription(),
        null,
        editorAgent,
        fileTypeRegistry);
    this.openImplementationPresenter = openImplementationPresenter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    EditorPartPresenter editor = editorAgent.getActiveEditor();
    if (editor instanceof TextEditor) {
      openImplementationPresenter.show(editor);
    } else {
      throw new IllegalStateException(
          getTemplatePresentation().getText()
              + " can be performed only on editors that implement TextEditor interface. Try to open this file"
              + " in another editor.");
    }
  }
}
