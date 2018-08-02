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
package org.eclipse.che.ide.command.editor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;

/**
 * Provides {@link CommandEditor} instances.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandEditorProvider implements EditorProvider {

  private final Provider<CommandEditor> editorProvider;
  private final EditorMessages editorMessages;

  @Inject
  public CommandEditorProvider(
      Provider<CommandEditor> editorProvider, EditorMessages editorMessages) {
    this.editorProvider = editorProvider;
    this.editorMessages = editorMessages;
  }

  @Override
  public String getId() {
    return "che_command_editor";
  }

  @Override
  public String getDescription() {
    return editorMessages.editorDescription();
  }

  @Override
  public EditorPartPresenter getEditor() {
    return editorProvider.get();
  }
}
