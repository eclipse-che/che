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
package org.eclipse.che.plugin.languageserver.ide.navigation.declaration;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.location.OpenLocationPresenter;
import org.eclipse.che.plugin.languageserver.ide.location.OpenLocationPresenterFactory;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentPositionParams;

/** @author Evgen Vidolob */
@Singleton
public class FindDefinitionAction extends AbstractPerspectiveAction {

  private final EditorAgent editorAgent;
  private final TextDocumentServiceClient client;
  private final DtoBuildHelper dtoBuildHelper;
  private final OpenLocationPresenter presenter;

  @Inject
  public FindDefinitionAction(
      EditorAgent editorAgent,
      OpenLocationPresenterFactory presenterFactory,
      TextDocumentServiceClient client,
      DtoBuildHelper dtoBuildHelper) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), "Find Definition", "Find Definition");
    this.editorAgent = editorAgent;
    this.client = client;
    this.dtoBuildHelper = dtoBuildHelper;
    presenter = presenterFactory.create("Find Definition");
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor instanceof TextEditor) {
      TextEditorConfiguration configuration = ((TextEditor) activeEditor).getConfiguration();
      if (configuration instanceof LanguageServerEditorConfiguration) {
        ServerCapabilities capabilities =
            ((LanguageServerEditorConfiguration) configuration).getServerCapabilities();
        event
            .getPresentation()
            .setEnabledAndVisible(
                capabilities.getDefinitionProvider() != null
                    && capabilities.getDefinitionProvider());
        return;
      }
    }
    event.getPresentation().setEnabledAndVisible(false);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();

    TextEditor textEditor = ((TextEditor) activeEditor);
    TextDocumentPositionParams paramsDTO =
        dtoBuildHelper.createTDPP(textEditor.getDocument(), textEditor.getCursorPosition());

    final Promise<List<Location>> promise = client.definition(paramsDTO);
    promise
        .then(
            arg -> {
              if (arg.size() == 1) {
                presenter.onLocationSelected(arg.get(0));
              } else {
                presenter.openLocation(promise);
              }
            })
        .catchError(
            arg -> {
              presenter.showError(arg);
            });
  }
}
