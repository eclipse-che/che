/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.editor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.editorconfig.DefaultTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.api.editor.reconciler.DefaultReconciler;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.signature.SignatureHelpProvider;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.LanguageServerQuickAssistProcessor;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.LanguageServerQuickAssistProcessorFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.signature.LanguageServerSignatureHelpFactory;
import org.eclipse.lsp4j.ServerCapabilities;

/** Configure editor with LS support */
public class LanguageServerEditorConfiguration extends DefaultTextEditorConfiguration {

  public static final int INITIAL_DOCUMENT_VERSION = 0;

  private final ServerCapabilities serverCapabilities;
  private final AnnotationModel annotationModel;
  private final DefaultReconciler reconciler;
  private final LanguageServerCodeassistProcessorFactory codeAssistProcessorFactory;
  private final SignatureHelpProvider signatureHelpProvider;
  private final LanguageServerFormatter formatter;
  private final LanguageServerQuickAssistProcessor quickAssistProcessor;

  @Inject
  public LanguageServerEditorConfiguration(
      @Assisted TextEditor editor,
      LanguageServerCodeassistProcessorFactory codeAssistProcessor,
      LanguageServerQuickAssistProcessorFactory quickAssistProcessorFactory,
      Provider<DocumentPositionMap> docPositionMapProvider,
      LanguageServerAnnotationModelFactory annotationModelFactory,
      LanguageServerReconcileStrategyFactory reconcileStrategyProviderFactory,
      LanguageServerFormatterFactory formatterFactory,
      LanguageServerSignatureHelpFactory signatureHelpFactory,
      @Assisted ServerCapabilities serverCapabilities) {
    codeAssistProcessorFactory = codeAssistProcessor;
    quickAssistProcessor = quickAssistProcessorFactory.create(editor);
    if ((serverCapabilities.getDocumentFormattingProvider() != null
            && serverCapabilities.getDocumentFormattingProvider())
        || (serverCapabilities.getDocumentRangeFormattingProvider() != null
            && serverCapabilities.getDocumentRangeFormattingProvider())
        || serverCapabilities.getDocumentOnTypeFormattingProvider() != null) {
      this.formatter = formatterFactory.create(serverCapabilities);
    } else {
      this.formatter = null;
    }
    this.serverCapabilities = serverCapabilities;
    DocumentPositionMap documentPositionMap = docPositionMapProvider.get();
    documentPositionMap.addPositionCategory(DocumentPositionMap.Categories.DEFAULT_CATEGORY);
    this.annotationModel = annotationModelFactory.get(documentPositionMap);

    this.reconciler =
        new DefaultReconciler(DocumentPartitioner.DEFAULT_CONTENT_TYPE, getPartitioner());
    reconciler.addReconcilingStrategy(
        DocumentPartitioner.DEFAULT_CONTENT_TYPE,
        reconcileStrategyProviderFactory.build(serverCapabilities));
    if (serverCapabilities.getSignatureHelpProvider() != null) {
      signatureHelpProvider = signatureHelpFactory.create(serverCapabilities);
    } else {
      signatureHelpProvider = null;
    }
  }

  @Override
  public Map<String, CodeAssistProcessor> getContentAssistantProcessors() {
    if (serverCapabilities.getCompletionProvider() != null) {
      Map<String, CodeAssistProcessor> map = new HashMap<>();
      map.put(
          DocumentPartitioner.DEFAULT_CONTENT_TYPE,
          codeAssistProcessorFactory.create(serverCapabilities));
      return map;
    }

    return null;
  }

  @Override
  public QuickAssistProcessor getQuickAssistProcessor() {
    return quickAssistProcessor;
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

  public ServerCapabilities getServerCapabilities() {
    return serverCapabilities;
  }

  @Override
  public SignatureHelpProvider getSignatureHelpProvider() {
    return signatureHelpProvider;
  }
}
