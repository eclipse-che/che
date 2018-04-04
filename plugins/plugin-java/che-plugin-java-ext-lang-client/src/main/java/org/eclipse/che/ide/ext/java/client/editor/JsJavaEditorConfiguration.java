/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.editor;

import static org.eclipse.che.ide.api.editor.partition.DefaultPartitioner.DEFAULT_PARTITIONING;
import static org.eclipse.che.ide.api.editor.partition.DocumentPartitioner.DEFAULT_CONTENT_TYPE;

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.changeintercept.ChangeInterceptorProvider;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.editorconfig.DefaultTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilerFactory;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/** Text editor configuration for java files. */
public class JsJavaEditorConfiguration extends DefaultTextEditorConfiguration {

  private final Map<String, CodeAssistProcessor> codeAssistProcessors;
  private final Reconciler reconciler;
  private final DocumentPartitioner partitioner;
  private final DocumentPositionMap documentPositionMap;
  private final AnnotationModel annotationModel;
  private final QuickAssistProcessor quickAssistProcessors;
  private final ChangeInterceptorProvider changeInterceptors;
  private final ContentFormatter contentFormatter;

  @AssistedInject
  public JsJavaEditorConfiguration(
      @Assisted final TextEditor editor,
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

    final JavaReconcilerStrategy javaReconcilerStrategy =
        strategyFactory.create(editor, codeAssistProcessor, this.annotationModel);

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

  private Reconciler initReconciler(
      final ReconcilerFactory reconcilerFactory,
      final JavaReconcilerStrategy javaReconcilerStrategy) {
    final Reconciler reconciler = reconcilerFactory.create(DEFAULT_PARTITIONING, getPartitioner());
    reconciler.addReconcilingStrategy(DEFAULT_CONTENT_TYPE, javaReconcilerStrategy);
    return reconciler;
  }
}
