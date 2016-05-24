package org.eclipse.che.plugin.languageserver.ide;

import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorProvider;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.DidCloseTextDocumentParamsDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.DidOpenTextDocumentParamsDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.DidSaveTextDocumentParamsDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.TextDocumentIdentifierDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.TextDocumentItemDTOImpl;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

@Extension(title = "LanguageServer")
public class LanguageServerExtension {

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
    
    @Inject protected void registerFileEventHandler(EventBus eventBus, final TextDocumentServiceClient serviceClient) {
        eventBus.addHandler(FileEvent.TYPE, new FileEventHandler() {
            
            @Override
            public void onFileOperation(FileEvent event) {
                TextDocumentIdentifierDTOImpl documentId = DtoClientImpls.TextDocumentIdentifierDTOImpl.make();
                documentId.setUri(event.getFile().getPath());
                switch (event.getOperationType()) {
                case OPEN:
                    DidOpenTextDocumentParamsDTOImpl openEvent = DtoClientImpls.DidOpenTextDocumentParamsDTOImpl.make();
                    TextDocumentItemDTOImpl documentItem = DtoClientImpls.TextDocumentItemDTOImpl.make();
                    documentItem.setUri(event.getFile().getPath());
                    documentItem.setVersion(LanguageServerEditorConfiguration.INITIAL_DOCUMENT_VERSION);
                    //TODO send text?
                    openEvent.setTextDocument(documentItem);
                    serviceClient.didOpen(openEvent);
                    break;
                case CLOSE:
                    DidCloseTextDocumentParamsDTOImpl closeEvent = DtoClientImpls.DidCloseTextDocumentParamsDTOImpl.make();
                    closeEvent.setTextDocument(documentId);
                    serviceClient.didClose(closeEvent);
                    break;
                case SAVE:
                    DidSaveTextDocumentParamsDTOImpl saveEvent = DtoClientImpls.DidSaveTextDocumentParamsDTOImpl.make();
                    saveEvent.setTextDocument(documentId);
                    serviceClient.didSave(saveEvent);
                    break;
                }
            }
        });
    }
}
