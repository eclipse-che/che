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
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.ApplyWorkspaceEditAction;

/**
 * Organizes the imports of a compilation unit.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class OrganizeImportsAction extends JavaEditorAction implements ProposalAction {
  public static final String JAVA_ORGANIZE_IMPORT_ID = "javaOrganizeImports";

  private final EditorAgent editorAgent;
  private final JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient;
  private final ApplyWorkspaceEditAction applyWorkspaceEditAction;

  @Inject
  public OrganizeImportsAction(
      JavaLocalizationConstant locale,
      EditorAgent editorAgent,
      FileTypeRegistry fileTypeRegistry,
      JavaLanguageExtensionServiceClient javaLanguageExtensionServiceClient,
      ApplyWorkspaceEditAction applyWorkspaceEditAction) {
    super(
        locale.organizeImportsName(),
        locale.organizeImportsDescription(),
        null,
        editorAgent,
        fileTypeRegistry);
    this.editorAgent = editorAgent;
    this.javaLanguageExtensionServiceClient = javaLanguageExtensionServiceClient;
    this.applyWorkspaceEditAction = applyWorkspaceEditAction;
  }

  @Override
  public void performAsProposal() {
    actionPerformed(null);
  }

  @Override
  public String getId() {
    return JAVA_ORGANIZE_IMPORT_ID;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final EditorPartPresenter editor = editorAgent.getActiveEditor();
    doOrganizeImports(editor);
  }

  private void doOrganizeImports(EditorPartPresenter editor) {
    if (!(editor instanceof TextEditor)) {
      return;
    }

    VirtualFile file = editor.getEditorInput().getFile();
    if (!(file instanceof Resource)) {
      return;
    }

    javaLanguageExtensionServiceClient
        .organizeImports(file.getLocation().toString())
        .then(applyWorkspaceEditAction::applyWorkspaceEdit)
        .catchError(
            error -> {
              Log.error(getClass(), error.getCause());
            });
  }
}
