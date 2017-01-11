/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.languageserver.shared.lsapi.DidCloseTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DidOpenTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DidSaveTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentItemDTO;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.input.KeyCodeMap;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.navigation.declaration.FindDefinitionAction;
import org.eclipse.che.plugin.languageserver.ide.navigation.references.FindReferencesAction;
import org.eclipse.che.plugin.languageserver.ide.navigation.symbol.GoToSymbolAction;
import org.eclipse.che.plugin.languageserver.ide.navigation.workspace.FindSymbolAction;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_ASSISTANT;

@Extension(title = "LanguageServer")
@Singleton
public class LanguageServerExtension {
    private final String GROUP_ASSISTANT_REFACTORING = "assistantRefactoringGroup";

    @Inject
    protected void injectCss(LanguageServerResources resources) {
        // we need to call this method one time
        resources.css().ensureInjected();
        resources.quickOpenListCss().ensureInjected();
    }

    @Inject
    protected void registerAction(ActionManager actionManager,
                                  KeyBindingAgent keyBindingManager,
                                  GoToSymbolAction goToSymbolAction,
                                  FindSymbolAction findSymbolAction,
                                  FindDefinitionAction findDefinitionAction,
                                  FindReferencesAction findReferencesAction) {
        actionManager.registerAction("LSGoToSymbolAction", goToSymbolAction);
        actionManager.registerAction("LSFindSymbolAction", findSymbolAction);
        actionManager.registerAction("LSFindDefinitionAction", findDefinitionAction);
        actionManager.registerAction("LSFindReferencesAction", findReferencesAction);

        DefaultActionGroup assistantGroup = (DefaultActionGroup)actionManager.getAction(GROUP_ASSISTANT);
        assistantGroup.add(goToSymbolAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
        assistantGroup.add(findSymbolAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
        assistantGroup.add(findDefinitionAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));
        assistantGroup.add(findReferencesAction, new Constraints(Anchor.BEFORE, GROUP_ASSISTANT_REFACTORING));


        if (UserAgent.isMac()) {
            keyBindingManager.getGlobal().addKey(new KeyBuilder().control().charCode(KeyCodeMap.F12).build(), "LSGoToSymbolAction");
        } else {
            keyBindingManager.getGlobal().addKey(new KeyBuilder().action().charCode(KeyCodeMap.F12).build(), "LSGoToSymbolAction");
        }
        keyBindingManager.getGlobal().addKey(new KeyBuilder().alt().charCode('n').build(),"LSFindSymbolAction");
        keyBindingManager.getGlobal().addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F7).build(),"LSFindReferencesAction");
        keyBindingManager.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F4).build(),"LSFindDefinitionAction");

    }

    @Inject
    protected void registerFileEventHandler(final EventBus eventBus,
                                            final TextDocumentServiceClient serviceClient,
                                            final DtoFactory dtoFactory,
                                            final LanguageServerFileTypeRegister fileTypeRegister) {
        eventBus.addHandler(FileEvent.TYPE, new FileEvent.FileEventHandler() {

            @Override
            public void onFileOperation(final FileEvent event) {
                Path location = event.getFile().getLocation();
                if (location.getFileExtension() == null || !fileTypeRegister.hasLSForExtension(location.getFileExtension())) {
                    return;
                }
                final TextDocumentIdentifierDTO documentId = dtoFactory.createDto(TextDocumentIdentifierDTO.class);
                documentId.setUri(location.toString());
                switch (event.getOperationType()) {
                    case OPEN:
                        onOpen(event, dtoFactory, serviceClient, fileTypeRegister);
                        break;
                    case CLOSE:
                        onClose(documentId, dtoFactory, serviceClient);
                        break;
                    case SAVE:
                        onSave(documentId, dtoFactory, serviceClient);
                        break;
                }
            }
        });
    }

    private void onSave(TextDocumentIdentifierDTO documentId,
                        DtoFactory dtoFactory,
                        TextDocumentServiceClient serviceClient) {
        DidSaveTextDocumentParamsDTO saveEvent = dtoFactory.createDto(DidSaveTextDocumentParamsDTO.class);
        saveEvent.setTextDocument(documentId);
        serviceClient.didSave(saveEvent);
    }

    private void onClose(TextDocumentIdentifierDTO documentId,
                         DtoFactory dtoFactory,
                         TextDocumentServiceClient serviceClient) {
        DidCloseTextDocumentParamsDTO closeEvent = dtoFactory.createDto(DidCloseTextDocumentParamsDTO.class);
        closeEvent.setTextDocument(documentId);
        serviceClient.didClose(closeEvent);
    }

    private void onOpen(final FileEvent event,
                        final DtoFactory dtoFactory,
                        final TextDocumentServiceClient serviceClient,
                        final LanguageServerFileTypeRegister fileTypeRegister) {
        event.getFile().getContent().then(new Operation<String>() {
            @Override
            public void apply(String text) throws OperationException {
                TextDocumentItemDTO documentItem = dtoFactory.createDto(TextDocumentItemDTO.class);
                documentItem.setUri(event.getFile().getLocation().toString());
                documentItem.setVersion(LanguageServerEditorConfiguration.INITIAL_DOCUMENT_VERSION);
                documentItem.setText(text);
                documentItem.setLanguageId(fileTypeRegister.findLangId(event.getFile().getLocation().getFileExtension()));

                DidOpenTextDocumentParamsDTO openEvent = dtoFactory.createDto(DidOpenTextDocumentParamsDTO.class);
                openEvent.setTextDocument(documentItem);
                openEvent.setUri(event.getFile().getLocation().toString());
                openEvent.setText(text);

                serviceClient.didOpen(openEvent);
            }
        });
    }
}
