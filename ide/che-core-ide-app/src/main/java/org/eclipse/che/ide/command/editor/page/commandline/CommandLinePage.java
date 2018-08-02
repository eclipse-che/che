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
package org.eclipse.che.ide.command.editor.page.commandline;

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
 * Presenter for {@link CommandEditorPage} which allows to edit command line.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandLinePage extends AbstractPageWithTextEditor {

  @Inject
  public CommandLinePage(
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
        messages.pageCommandLineTitle(),
        editorConfiguration);

    view.asWidget().getElement().setId("command_editor-command_line");
  }

  @Override
  protected String getCommandPropertyValue() {
    return editedCommand.getCommandLine();
  }

  @Override
  protected void updateCommandPropertyValue(String content) {
    editedCommand.setCommandLine(content);
  }

  @Override
  protected String getType() {
    return ".sh";
  }
}
