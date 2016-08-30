package org.eclipse.che.plugin.languageserver.ide.editor;

import io.typefox.lsapi.ServerCapabilities;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.editorconfig.DefaultTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilerWithAutoSave;

import java.util.HashMap;
import java.util.Map;

public class LanguageServerEditorConfiguration extends DefaultTextEditorConfiguration {

    public static final int INITIAL_DOCUMENT_VERSION = 0;

    private final ServerCapabilities                serverCapabilities;
    private final AnnotationModel                   annotationModel;
    private final ReconcilerWithAutoSave            reconciler;
    private final LanguageServerCodeassistProcessorFactory codeAssistProcessorFactory;

    private LanguageServerFormatter formatter;

    @Inject
    public LanguageServerEditorConfiguration(final LanguageServerCodeassistProcessorFactory codeAssistProcessor,
                                             final Provider<DocumentPositionMap> docPositionMapProvider,
                                             final LanguageServerAnnotationModelFactory annotationModelFactory,
                                             final LanguageServerReconcileStrategyFactory reconcileStrategyProviderFactory,
                                             final LanguageServerFormatterFactory formatterFactory,
                                             @Assisted ServerCapabilities serverCapabilities) {
        codeAssistProcessorFactory = codeAssistProcessor;
        if (serverCapabilities.isDocumentFormattingProvider() || serverCapabilities.isDocumentRangeFormattingProvider() ||
            serverCapabilities.getDocumentOnTypeFormattingProvider() != null) {
            this.formatter = formatterFactory.create(serverCapabilities);
            formatter.setTabWidth(getTabWidth());
        }
        this.serverCapabilities = serverCapabilities;
        this.annotationModel = annotationModelFactory.get(docPositionMapProvider.get());

        this.reconciler = new ReconcilerWithAutoSave(DocumentPartitioner.DEFAULT_CONTENT_TYPE, getPartitioner());
        reconciler.addReconcilingStrategy(DocumentPartitioner.DEFAULT_CONTENT_TYPE, reconcileStrategyProviderFactory.build(serverCapabilities));
    }

    @Override
    public Map<String, CodeAssistProcessor> getContentAssistantProcessors() {
        if (serverCapabilities.getCompletionProvider() != null) {
            Map<String, CodeAssistProcessor> map = new HashMap<>();
            map.put(DocumentPartitioner.DEFAULT_CONTENT_TYPE, codeAssistProcessorFactory.create(serverCapabilities));
            return map;
        }

        return null;
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
