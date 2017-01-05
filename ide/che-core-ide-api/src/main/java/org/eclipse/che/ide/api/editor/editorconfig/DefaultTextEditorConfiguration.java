/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.editorconfig;

import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.changeintercept.ChangeInterceptorProvider;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.partition.ConstantPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.signature.SignatureHelpProvider;

import java.util.Map;

/**
 * Default implementation of the {@link TextEditorConfiguration}.
 */
public class DefaultTextEditorConfiguration implements TextEditorConfiguration {

    private ConstantPartitioner partitioner;

    @Override
    public int getTabWidth() {
        return 3;
    }

    @Override
    public ContentFormatter getContentFormatter() {
        return null;
    }

    @Override
    public Map<String, CodeAssistProcessor> getContentAssistantProcessors() {
        return null;
    }

    @Override
    public Reconciler getReconciler() {
        return null;
    }

    @Override
    public DocumentPartitioner getPartitioner() {
        if(partitioner == null) {
            partitioner = new ConstantPartitioner();
        }
        return partitioner;
    }

    @Override
    public AnnotationModel getAnnotationModel() {
        return null;
    }

    @Override
    public DocumentPositionMap getDocumentPositionMap() {
        return null;
    }

    @Override
    public QuickAssistProcessor getQuickAssistProcessor() {
        return null;
    }

    @Override
    public ChangeInterceptorProvider getChangeInterceptorProvider() {
        return null;
    }

    @Override
    public SignatureHelpProvider getSignatureHelpProvider() {
        return null;
    }
}
