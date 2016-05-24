package org.eclipse.che.plugin.languageserver.ide.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModelImpl;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.editorconfig.DefaultTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.events.DocumentChangeEvent;
import org.eclipse.che.ide.api.editor.partition.ConstantPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilerWithAutoSave;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.plugin.languageserver.ide.editor.codeassist.LanguageServerCodeAssistProcessor;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.DidChangeTextDocumentParamsDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.PositionDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.RangeDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.TextDocumentContentChangeEventDTOImpl;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoClientImpls.VersionedTextDocumentIdentifierDTOImpl;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class LanguageServerEditorConfiguration extends DefaultTextEditorConfiguration {

    public static final int INITIAL_DOCUMENT_VERSION = 0;
    
    private final LanguageServerCodeAssistProcessor codeAssistProcessor;
    private final AnnotationModel annotationModel;
    private final DocumentPositionMap documentPositionMap;
    private final AtomicInteger version = new AtomicInteger(INITIAL_DOCUMENT_VERSION);
    private final ConstantPartitioner constantPartitioner;
    private final ReconcilerWithAutoSave reconciler;

    @Inject
    public LanguageServerEditorConfiguration(final LanguageServerCodeAssistProcessor codeAssistProcessor, final Provider<DocumentPositionMap> docPositionMapProvider, final TextDocumentServiceClient textDocumentService) {
        this.codeAssistProcessor = codeAssistProcessor;
        this.documentPositionMap = docPositionMapProvider.get();
        this.annotationModel = new AnnotationModelImpl(documentPositionMap);
        
        //HACK hijacked the partitioner to get document change events
        this.constantPartitioner = new ConstantPartitioner() {
            
            @Override
            public void onDocumentChange(DocumentChangeEvent event) {
                super.onDocumentChange(event);
                
                Document document = event.getDocument().getDocument();
                TextPosition startPosition = document.getPositionFromIndex(event.getOffset());
                TextPosition endPosition = document.getPositionFromIndex(event.getOffset() + event.getLength());
                
                DidChangeTextDocumentParamsDTOImpl changeDTO = DtoClientImpls.DidChangeTextDocumentParamsDTOImpl.make();
                String uri = ((Document) document).getFile().getPath();
                changeDTO.setUri(uri);
                VersionedTextDocumentIdentifierDTOImpl versionedDocId = DtoClientImpls.VersionedTextDocumentIdentifierDTOImpl.make();
                versionedDocId.setUri(uri);
                versionedDocId.setVersion(version.incrementAndGet());
                changeDTO.setTextDocument(versionedDocId);
                TextDocumentContentChangeEventDTOImpl actualChange = DtoClientImpls.TextDocumentContentChangeEventDTOImpl.make();
                RangeDTOImpl range = DtoClientImpls.RangeDTOImpl.make();
                PositionDTOImpl start = DtoClientImpls.PositionDTOImpl.make();
                start.setLine(startPosition.getLine());
                start.setCharacter(startPosition.getCharacter());
                PositionDTOImpl end = DtoClientImpls.PositionDTOImpl.make();
                end.setLine(endPosition.getLine());
                end.setCharacter(endPosition.getCharacter());
                range.setStart(start);
                range.setEnd(end);
                actualChange.setRange(range);
                actualChange.setText(event.getText());
                changeDTO.addContentChanges(actualChange);
                textDocumentService.didChange(changeDTO);
            }
        };
        this.reconciler = new ReconcilerWithAutoSave(DocumentPartitioner.DEFAULT_CONTENT_TYPE, constantPartitioner);
    }

    @Override
    public Map<String, CodeAssistProcessor> getContentAssistantProcessors() {
        Map<String, CodeAssistProcessor> map = new HashMap<>();
        map.put(DocumentPartitioner.DEFAULT_CONTENT_TYPE, codeAssistProcessor);
        return map;
    }
    
    @Override
    public AnnotationModel getAnnotationModel() {
        return annotationModel;
    }
    
    @Override
    public DocumentPartitioner getPartitioner() {
        return constantPartitioner;
    }
    
    @Override
    public Reconciler getReconciler() {
        return reconciler;
    }
    
}
