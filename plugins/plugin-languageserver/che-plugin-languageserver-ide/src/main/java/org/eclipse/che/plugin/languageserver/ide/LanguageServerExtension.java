package org.eclipse.che.plugin.languageserver.ide;

import static com.google.common.collect.Lists.newArrayList;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.editor.filetype.MultipleMethodFileIdentifier;
import org.eclipse.che.ide.api.editor.texteditor.EditorModule.EditorModuleReadyCallback;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.editor.orion.client.OrionContentTypeRegistrant;
import org.eclipse.che.ide.editor.orion.client.OrionEditorModule;
import org.eclipse.che.ide.editor.orion.client.jso.OrionCodeEditWidgetOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionContentTypeOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionHighlightingConfigurationOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionServiceRegistryOverlay;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorProvider;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidCloseTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidOpenTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidSaveTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentItemDTO;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

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
            final EditorRegistry editorRegistry, final LanguageServerEditorProvider editorProvider, 
            final OrionContentTypeRegistrant contentTypeRegistrant) {
        // TODO the file types need to be retrieved from the server. Ideally we
        // would listen on messages when new language servers get registered.
        final FileType fileType = new FileType(resources.file(), "foo");
        fileTypeRegistry.registerFileType(fileType);
        final FileType fileType2 = new FileType(resources.file(), "testlang");
        fileTypeRegistry.registerFileType(fileType2);
        
        // register editor provider
        editorRegistry.registerDefaultEditor(fileType, editorProvider);
        editorRegistry.registerDefaultEditor(fileType2, editorProvider);
        
        // register content type and configure orion
        final String contentTypeId = "text/x-testlang";
        
        OrionContentTypeOverlay contentType = OrionContentTypeOverlay.create();
        contentType.setId(contentTypeId);
        contentType.setName("Test Language");
        contentType.setExtension("testlang");
        contentType.setExtends("text/plain");
        
        // highlighting
        OrionHighlightingConfigurationOverlay config = OrionHighlightingConfigurationOverlay.create();
        config.setId("testlang.highlighting");
        config.setContentTypes(contentTypeId);
        config.setPatterns(
                "[\n" + 
                        "  {include: \"orion.lib#string_doubleQuote\"},\n" + 
                        "  {include: \"orion.lib#string_singleQuote\"},\n" + 
                        "  {include: \"orion.lib#brace_open\"},\n" + 
                        "  {include: \"orion.lib#brace_close\"},\n" + 
                        "  {include: \"orion.lib#bracket_open\"},\n" + 
                        "  {include: \"orion.lib#bracket_close\"},\n" + 
                        "  {include: \"orion.lib#parenthesis_open\"},\n" + 
                        "  {include: \"orion.lib#parenthesis_close\"},\n" + 
                        "  {include: \"orion.lib#number_decimal\"},\n" + 
                        "  {include: \"orion.lib#number_hex\"},\n" + 
                        "  {\n" + 
                        "    match: \"\\\\b(?:false|true)\\\\b\",\n" + 
                        "    name: \"keyword.json\"\n" + 
                        "  }\n" + 
                "]");
        
        contentTypeRegistrant.registerFileType(contentType, config);
    }
    
    @Inject
    protected void registerFileEventHandler(EventBus eventBus, final TextDocumentServiceClient serviceClient, final DtoFactory dtoFactory) {
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
                            DidOpenTextDocumentParamsDTO openEvent = dtoFactory.createDto(DidOpenTextDocumentParamsDTO.class);
                            TextDocumentItemDTO documentItem = dtoFactory.createDto(TextDocumentItemDTO.class);
                            documentItem.setUri(event.getFile().getPath());
                            documentItem.setVersion(LanguageServerEditorConfiguration.INITIAL_DOCUMENT_VERSION);
                            documentItem.setText(text);
                            openEvent.setTextDocument(documentItem);
                            serviceClient.didOpen(openEvent);
                        }});
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
