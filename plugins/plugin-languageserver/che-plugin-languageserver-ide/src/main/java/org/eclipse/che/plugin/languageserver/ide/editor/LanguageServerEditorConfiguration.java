package org.eclipse.che.plugin.languageserver.ide.editor;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.editorconfig.DefaultTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilerWithAutoSave;
import org.eclipse.che.plugin.languageserver.ide.editor.codeassist.LanguageServerCodeAssistProcessor;

import java.util.HashMap;
import java.util.Map;

public class LanguageServerEditorConfiguration extends DefaultTextEditorConfiguration {

    public static final int INITIAL_DOCUMENT_VERSION = 0;
    
    private final LanguageServerCodeAssistProcessor codeAssistProcessor;
    private final LanguageServerFormatter formatter;
    private final AnnotationModel annotationModel;
    private final ReconcilerWithAutoSave reconciler;

    @Inject
    public LanguageServerEditorConfiguration(final LanguageServerCodeAssistProcessor codeAssistProcessor,
                                             final Provider<DocumentPositionMap> docPositionMapProvider,
                                             final LanguageServerAnnotationModelFactory annotationModelFactory,
                                             final Provider<LanguageServerReconcileStrategy> reconcileStrategyProvider,
                                             final LanguageServerFormatter formatter) {
        this.codeAssistProcessor = codeAssistProcessor;
        this.formatter = formatter;
        formatter.setTabWidth(getTabWidth());
        this.annotationModel = annotationModelFactory.get(docPositionMapProvider.get());

        this.reconciler = new ReconcilerWithAutoSave(DocumentPartitioner.DEFAULT_CONTENT_TYPE, getPartitioner());
        reconciler.addReconcilingStrategy(DocumentPartitioner.DEFAULT_CONTENT_TYPE, reconcileStrategyProvider.get());
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
    public Reconciler getReconciler() {
        return reconciler;
    }

    @Override
    public ContentFormatter getContentFormatter() {
        return formatter;
    }
}
