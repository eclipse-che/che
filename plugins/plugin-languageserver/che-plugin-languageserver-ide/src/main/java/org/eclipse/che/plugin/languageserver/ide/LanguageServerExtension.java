package org.eclipse.che.plugin.languageserver.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorProvider;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidCloseTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidOpenTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidSaveTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentItemDTO;

@Extension(title = "LanguageServer")
@Singleton
public class LanguageServerExtension {

    @Inject
    protected void injectCss(LanguageServerResources resources) {
        //we need to call this method one time
        resources.css().ensureInjected();
    }

    @Inject
    protected void configureFileTypes(FileTypeRegistry fileTypeRegistry, LanguageServerResources resources,
            final EditorRegistry editorRegistry, final LanguageServerEditorProvider editorProvider) {
        // TODO the file types need to be retrieved from the server. Ideally we
        // would listen on messages when new language servers get registered.
        FileType fileType = new FileType(resources.file(), "foo");
        fileTypeRegistry.registerFileType(fileType);
        // register editor provider
        editorRegistry.registerDefaultEditor(fileType, editorProvider);
    }
    
    @Inject
    protected void registerFileEventHandler(EventBus eventBus, final TextDocumentServiceClient serviceClient, final DtoFactory dtoFactory) {
        eventBus.addHandler(FileEvent.TYPE, new FileEventHandler() {
            
            @Override
            public void onFileOperation(FileEvent event) {
                TextDocumentIdentifierDTO documentId = dtoFactory.createDto(TextDocumentIdentifierDTO.class);
                documentId.setUri(event.getFile().getPath());
                switch (event.getOperationType()) {
                case OPEN:
                    DidOpenTextDocumentParamsDTO openEvent = dtoFactory.createDto(DidOpenTextDocumentParamsDTO.class);
                    TextDocumentItemDTO documentItem = dtoFactory.createDto(TextDocumentItemDTO.class);
                    documentItem.setUri(event.getFile().getPath());
                    documentItem.setVersion(LanguageServerEditorConfiguration.INITIAL_DOCUMENT_VERSION);
                    //TODO send text?
                    openEvent.setTextDocument(documentItem);
                    serviceClient.didOpen(openEvent);
                    break;
                case CLOSE:
                    DidCloseTextDocumentParamsDTO closeEvent = dtoFactory.createDto(DidCloseTextDocumentParamsDTO.class);
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
