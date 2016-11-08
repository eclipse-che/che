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
import org.eclipse.che.ide.api.editor.signature.SignatureHelpProvider;
import org.eclipse.che.plugin.languageserver.ide.editor.signature.LanguageServerSignatureHelpFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configure editor with LS support
 */
public class LanguageServerEditorConfiguration extends DefaultTextEditorConfiguration {

    public static final int INITIAL_DOCUMENT_VERSION = 0;

    private final ServerCapabilities                       serverCapabilities;
    private final AnnotationModel                          annotationModel;
    private final ReconcilerWithAutoSave                   reconciler;
    private final LanguageServerCodeassistProcessorFactory codeAssistProcessorFactory;
    private final SignatureHelpProvider                    signatureHelpProvider;
    private       LanguageServerFormatter                  formatter;

    @Inject
    public LanguageServerEditorConfiguration(LanguageServerCodeassistProcessorFactory codeAssistProcessor,
                                             Provider<DocumentPositionMap> docPositionMapProvider,
                                             LanguageServerAnnotationModelFactory annotationModelFactory,
                                             LanguageServerReconcileStrategyFactory reconcileStrategyProviderFactory,
                                             LanguageServerFormatterFactory formatterFactory,
                                             LanguageServerSignatureHelpFactory signatureHelpFactory,
                                             @Assisted ServerCapabilities serverCapabilities) {
        codeAssistProcessorFactory = codeAssistProcessor;
        if ((serverCapabilities.isDocumentFormattingProvider() != null && serverCapabilities.isDocumentFormattingProvider()) ||
            (serverCapabilities.isDocumentRangeFormattingProvider() != null && serverCapabilities.isDocumentRangeFormattingProvider()) ||
            serverCapabilities.getDocumentOnTypeFormattingProvider() != null) {
            this.formatter = formatterFactory.create(serverCapabilities);
        }
        this.serverCapabilities = serverCapabilities;
        this.annotationModel = annotationModelFactory.get(docPositionMapProvider.get());

        this.reconciler = new ReconcilerWithAutoSave(DocumentPartitioner.DEFAULT_CONTENT_TYPE, getPartitioner());
        reconciler.addReconcilingStrategy(DocumentPartitioner.DEFAULT_CONTENT_TYPE, reconcileStrategyProviderFactory.build(serverCapabilities));
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

    public ServerCapabilities getServerCapabilities() {
        return serverCapabilities;
    }

    @Override
    public SignatureHelpProvider getSignatureHelpProvider() {
        return signatureHelpProvider;
    }
}
