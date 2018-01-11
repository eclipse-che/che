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
package org.eclipse.che.ide.command.editor.page.previewurl;

import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.editor.defaulteditor.EditorBuilder;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;
import org.eclipse.che.ide.command.editor.page.text.AbstractPageWithTextEditor;
import org.eclipse.che.ide.command.editor.page.text.MacroEditorConfiguration;
import org.eclipse.che.ide.command.editor.page.text.PageWithTextEditorView;
import org.eclipse.che.ide.macro.chooser.MacroChooser;

/**
 * Presenter for {@link CommandEditorPage} which allows to edit command's preview URL.
 *
 * @author Artem Zatsarynnyi
 */
public class PreviewUrlPage extends AbstractPageWithTextEditor {

  @Inject
  public PreviewUrlPage(
      PageWithTextEditorView view,
      EditorBuilder editorBuilder,
      FileTypeRegistry fileTypeRegistry,
      MacroChooser macroChooser,
      EditorMessages messages,
      MacroEditorConfiguration editorConfiguration) {
    super(
        view,
        editorBuilder,
        fileTypeRegistry,
        macroChooser,
        messages.pagePreviewUrlTitle(),
        editorConfiguration);

    view.asWidget().getElement().setId("command_editor-preview_url");
  }

  @Override
  protected String getCommandPropertyValue() {
    final String previewUrl = editedCommand.getAttributes().get(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME);

    return previewUrl != null ? previewUrl : "";
  }

  @Override
  protected void updateCommandPropertyValue(String content) {
    editedCommand.getAttributes().put(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME, content);
  }
}
