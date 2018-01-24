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
package org.eclipse.che.plugin.languageserver.ide.navigation.references;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.location.OpenLocationPresenter;
import org.eclipse.che.plugin.languageserver.ide.location.OpenLocationPresenterFactory;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/** @author Evgen Vidolob */
public class FindReferencesAction extends AbstractPerspectiveAction {

  private final EditorAgent editorAgent;
  private final TextDocumentServiceClient client;
  private final DtoFactory dtoFactory;
  private final DtoBuildHelper dtoHelper;
  private final OpenLocationPresenter presenter;

  @Inject
  public FindReferencesAction(
      EditorAgent editorAgent,
      OpenLocationPresenterFactory presenterFactory,
      TextDocumentServiceClient client,
      DtoFactory dtoFactory,
      DtoBuildHelper dtoHelper) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), "Find References", "Find References");
    this.editorAgent = editorAgent;
    this.client = client;
    this.dtoFactory = dtoFactory;
    this.dtoHelper = dtoHelper;
    presenter = presenterFactory.create("Find References");
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
                capabilities.getReferencesProvider() != null
                    && capabilities.getReferencesProvider());
        return;
      }
    }
    event.getPresentation().setEnabledAndVisible(false);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();

    // TODO replace this
    if (!(activeEditor instanceof TextEditor)) {
      return;
    }
    TextEditor textEditor = ((TextEditor) activeEditor);
    ReferenceParams paramsDTO = dtoFactory.createDto(ReferenceParams.class);

    Position Position = dtoFactory.createDto(Position.class);
    Position.setLine(textEditor.getCursorPosition().getLine());
    Position.setCharacter(textEditor.getCursorPosition().getCharacter());

    TextDocumentIdentifier identifierDTO =
        dtoHelper.createTDI(activeEditor.getEditorInput().getFile());

    ReferenceContext contextDTO = dtoFactory.createDto(ReferenceContext.class);
    contextDTO.setIncludeDeclaration(true);

    paramsDTO.setUri(identifierDTO.getUri());
    paramsDTO.setPosition(Position);
    paramsDTO.setTextDocument(identifierDTO);
    paramsDTO.setContext(contextDTO);
    Promise<List<Location>> promise = client.references(paramsDTO);
    presenter.openLocation(promise);
  }
}
