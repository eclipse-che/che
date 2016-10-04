/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.navigation.declaration;

import io.typefox.lsapi.ServerCapabilities;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.languageserver.shared.lsapi.LocationDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
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

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class FindDefinitionAction extends AbstractPerspectiveAction {


    private final EditorAgent               editorAgent;
    private final TextDocumentServiceClient client;
    private final DtoBuildHelper            dtoBuildHelper;
    private final OpenLocationPresenter     presenter;

    @Inject
    public FindDefinitionAction(EditorAgent editorAgent, OpenLocationPresenterFactory presenterFactory,
                                TextDocumentServiceClient client, DtoBuildHelper dtoBuildHelper) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), "Find Definition", "Find Definition", null, null);
        this.editorAgent = editorAgent;
        this.client = client;
        this.dtoBuildHelper = dtoBuildHelper;
        presenter = presenterFactory.create("Find Definition");
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor instanceof TextEditor) {
            TextEditorConfiguration configuration = ((TextEditor)activeEditor).getConfiguration();
            if (configuration instanceof LanguageServerEditorConfiguration) {
                ServerCapabilities capabilities = ((LanguageServerEditorConfiguration)configuration).getServerCapabilities();
                event.getPresentation()
                     .setEnabledAndVisible(capabilities.isDefinitionProvider() != null && capabilities.isDefinitionProvider());
                return;
            }
        }
        event.getPresentation().setEnabledAndVisible(false);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();

        TextEditor textEditor = ((TextEditor)activeEditor);
        TextDocumentPositionParamsDTO paramsDTO = dtoBuildHelper.createTDPP(textEditor.getDocument(), textEditor.getCursorPosition());

        final Promise<List<LocationDTO>> promise = client.definition(paramsDTO);
        promise.then(new Operation<List<LocationDTO>>() {
            @Override
            public void apply(List<LocationDTO> arg) throws OperationException {
                if (arg.size() == 1) {
                    presenter.onLocationSelected(arg.get(0));
                } else {
                    presenter.openLocation(promise);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                presenter.showError(arg);
            }
        });
    }
}
