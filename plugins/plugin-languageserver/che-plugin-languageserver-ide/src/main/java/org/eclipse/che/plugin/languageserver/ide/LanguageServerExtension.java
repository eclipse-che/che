package org.eclipse.che.plugin.languageserver.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.editor.orion.client.OrionContentTypeRegistrant;
import org.eclipse.che.ide.editor.orion.client.jso.OrionContentTypeOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionHighlightingConfigurationOverlay;
import org.eclipse.che.ide.keybinding.KeyBindingManager;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.input.KeyCodeMap;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorProvider;
import org.eclipse.che.plugin.languageserver.ide.navigation.declaration.FindDefinitionAction;
import org.eclipse.che.plugin.languageserver.ide.navigation.references.FindReferencesAction;
import org.eclipse.che.plugin.languageserver.ide.navigation.symbol.GoToSymbolAction;
import org.eclipse.che.plugin.languageserver.ide.navigation.workspace.FindSymbolAction;
import org.eclipse.che.plugin.languageserver.ide.service.LanguageRegistryServiceClient;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidCloseTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidOpenTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidSaveTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.LanguageDescriptionDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentItemDTO;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
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
    protected void configureFileTypes(
            final FileTypeRegistry fileTypeRegistry,
            final LanguageServerResources resources,
            final EditorRegistry editorRegistry,
            final LanguageServerEditorProvider editorProvider,
            final OrionContentTypeRegistrant contentTypeRegistrant,
            final LanguageRegistryServiceClient serverLanguageRegistry,
            final EventBus eventBus) {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {

            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                Promise<List<LanguageDescriptionDTO>> registeredLanguages = serverLanguageRegistry.getRegisteredLanguages();
                registeredLanguages.then(new Operation<List<LanguageDescriptionDTO>>() {
                    @Override
                    public void apply(List<LanguageDescriptionDTO> langs) throws OperationException {
                        if (langs.isEmpty()) {
                            return;
                        }
                        for (LanguageDescriptionDTO lang : langs) {
                            String primaryExtension = lang.getFileExtensions().get(0);
                            for (String ext : lang.getFileExtensions()) {
                                final FileType fileType = new FileType(resources.file(), ext);
                                fileTypeRegistry.registerFileType(fileType);
                                editorRegistry.registerDefaultEditor(fileType, editorProvider);
                            }
                            List<String> mimeTypes = lang.getMimeTypes();
                            if (mimeTypes.isEmpty()) {
                                mimeTypes = newArrayList("text/x-" + lang.getLanguageId());
                            }
                            for (String contentTypeId : mimeTypes) {

                                OrionContentTypeOverlay contentType = OrionContentTypeOverlay.create();
                                contentType.setId(contentTypeId);
                                contentType.setName(lang.getLanguageId());
                                contentType.setExtension(primaryExtension);
                                contentType.setExtends("text/plain");

                                // highlighting
                                OrionHighlightingConfigurationOverlay config = OrionHighlightingConfigurationOverlay
                                        .create();
                                config.setId(lang.getLanguageId() + ".highlighting");
                                config.setContentTypes(contentTypeId);
                                config.setPatterns(lang.getHighlightingConfiguration());

                                contentTypeRegistrant.registerFileType(contentType, config);
                            }
                        }
                    }
                });
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Inject
    protected void registerAction(ActionManager actionManager,
                                  KeyBindingManager keyBindingManager,
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
    }

    @Inject
    protected void registerFileEventHandler(EventBus eventBus, final TextDocumentServiceClient serviceClient,
                                            final DtoFactory dtoFactory) {
        eventBus.addHandler(FileEvent.TYPE, new FileEventHandler() {

            @Override
            public void onFileOperation(final FileEvent event) {
                final TextDocumentIdentifierDTO documentId = dtoFactory.createDto(TextDocumentIdentifierDTO.class);
                documentId.setUri(event.getFile().getPath());
                switch (event.getOperationType()) {
                    case OPEN:
                        event.getFile().getContent().then(new Operation<String>() {
                            @Override
                            public void apply(String text) throws OperationException {
                                DidOpenTextDocumentParamsDTO openEvent = dtoFactory
                                        .createDto(DidOpenTextDocumentParamsDTO.class);
                                TextDocumentItemDTO documentItem = dtoFactory.createDto(TextDocumentItemDTO.class);
                                documentItem.setUri(event.getFile().getPath());
                                documentItem.setVersion(LanguageServerEditorConfiguration.INITIAL_DOCUMENT_VERSION);
                                documentItem.setText(text);
                                openEvent.setTextDocument(documentItem);
                                openEvent.setUri(event.getFile().getPath());
                                openEvent.setText(text);
                                serviceClient.didOpen(openEvent);
                            }
                        });
                        break;
                    case CLOSE:
                        DidCloseTextDocumentParamsDTO closeEvent = dtoFactory
                                .createDto(DidCloseTextDocumentParamsDTO.class);
                        closeEvent.setTextDocument(documentId);
                        serviceClient.didClose(closeEvent);
                        break;
                    case SAVE:
                        DidSaveTextDocumentParamsDTO saveEvent = dtoFactory.createDto(DidSaveTextDocumentParamsDTO.class);
                        saveEvent.setTextDocument(documentId);
                        serviceClient.didSave(saveEvent);
                        break;
                }
            }
        });
    }
}
