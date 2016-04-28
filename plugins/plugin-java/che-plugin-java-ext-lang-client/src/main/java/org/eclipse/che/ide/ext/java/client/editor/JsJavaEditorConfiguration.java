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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.jseditor.client.annotation.AnnotationModel;
import org.eclipse.che.ide.jseditor.client.changeintercept.ChangeInterceptorProvider;
import org.eclipse.che.ide.jseditor.client.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.jseditor.client.editorconfig.DefaultTextEditorConfiguration;
import org.eclipse.che.ide.jseditor.client.formatter.ContentFormatter;
import org.eclipse.che.ide.jseditor.client.partition.DocumentPartitioner;
import org.eclipse.che.ide.jseditor.client.partition.DocumentPositionMap;
import org.eclipse.che.ide.jseditor.client.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.jseditor.client.reconciler.Reconciler;
import org.eclipse.che.ide.jseditor.client.reconciler.ReconcilerFactory;
import org.eclipse.che.ide.jseditor.client.texteditor.TextEditorPresenter;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.jseditor.client.partition.DefaultPartitioner.DEFAULT_PARTITIONING;
import static org.eclipse.che.ide.jseditor.client.partition.DocumentPartitioner.DEFAULT_CONTENT_TYPE;

/**
 * Text editor configuration for java files.
 */
public class JsJavaEditorConfiguration extends DefaultTextEditorConfiguration {

    private final Map<String, CodeAssistProcessor> codeAssistProcessors;
    private final Reconciler                       reconciler;
    private final DocumentPartitioner              partitioner;
    private final DocumentPositionMap              documentPositionMap;
    private final AnnotationModel                  annotationModel;
    private final QuickAssistProcessor             quickAssistProcessors;
    private final ChangeInterceptorProvider        changeInterceptors;
    private final ContentFormatter                 contentFormatter;

    @AssistedInject
    public JsJavaEditorConfiguration(@Assisted final TextEditorPresenter<?> editor,
                                     final JavaCodeAssistProcessorFactory codeAssistProcessorFactory,
                                     final JavaQuickAssistProcessorFactory quickAssistProcessorFactory,
                                     final ReconcilerFactory reconcilerFactory,
                                     final JavaPartitionerFactory partitionerFactory,
                                     final JavaReconcilerStrategyFactory strategyFactory,
                                     final Provider<DocumentPositionMap> docPositionMapProvider,
                                     final JavaAnnotationModelFactory javaAnnotationModelFactory,
                                     final ContentFormatter contentFormatter) {
        this.contentFormatter = contentFormatter;

        final JavaCodeAssistProcessor codeAssistProcessor = codeAssistProcessorFactory.create(editor);
        this.codeAssistProcessors = new HashMap<>();
        this.codeAssistProcessors.put(DEFAULT_CONTENT_TYPE, codeAssistProcessor);
        this.quickAssistProcessors = quickAssistProcessorFactory.create(editor);

        this.documentPositionMap = docPositionMapProvider.get();
        this.annotationModel = javaAnnotationModelFactory.create(this.documentPositionMap);

        final JavaReconcilerStrategy javaReconcilerStrategy = strategyFactory.create(editor,
                                                                                     codeAssistProcessor,
                                                                                     this.annotationModel);

        this.partitioner = partitionerFactory.create(this.documentPositionMap);
        this.reconciler = initReconciler(reconcilerFactory, javaReconcilerStrategy);

        this.changeInterceptors = new JavaChangeInterceptorProvider();
    }

    @Override
    public Map<String, CodeAssistProcessor> getContentAssistantProcessors() {
        return this.codeAssistProcessors;
    }

    @Override
    public QuickAssistProcessor getQuickAssistProcessor() {
        return this.quickAssistProcessors;
    }

    @Override
    public Reconciler getReconciler() {
        return this.reconciler;
    }

    @Override
    public DocumentPositionMap getDocumentPositionMap() {
        return this.documentPositionMap;
    }

    @Override
    public AnnotationModel getAnnotationModel() {
        return this.annotationModel;
    }

    @Override
    public ContentFormatter getContentFormatter() {
        return contentFormatter;
    }

    @Override
    public ChangeInterceptorProvider getChangeInterceptorProvider() {
        return this.changeInterceptors;
    }

    private Reconciler initReconciler(final ReconcilerFactory reconcilerFactory,
                                      final JavaReconcilerStrategy javaReconcilerStrategy) {
        final Reconciler reconciler = reconcilerFactory.create(DEFAULT_PARTITIONING, getPartitioner());
        reconciler.addReconcilingStrategy(DEFAULT_CONTENT_TYPE, javaReconcilerStrategy);
        return reconciler;
    }
}
