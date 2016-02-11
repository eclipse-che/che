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
package org.eclipse.che.ide.jseditor.client.editorconfig;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.jseditor.client.annotation.AnnotationModel;
import org.eclipse.che.ide.jseditor.client.changeintercept.ChangeInterceptorProvider;
import org.eclipse.che.ide.jseditor.client.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.jseditor.client.formatter.ContentFormatter;
import org.eclipse.che.ide.jseditor.client.partition.DocumentPartitioner;
import org.eclipse.che.ide.jseditor.client.partition.DocumentPositionMap;
import org.eclipse.che.ide.jseditor.client.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.jseditor.client.reconciler.Reconciler;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Configure extended functions of the editor.
 */
public interface TextEditorConfiguration {

    /**
     * Returns the visual width of the tab character. This implementation always returns 3.
     *
     * @return the tab width
     */
    public int getTabWidth();

    /**
     * Returns the content formatter.
     *
     * @return the content formatter
     */
    @Nullable
    public ContentFormatter getContentFormatter();

    /**
     * Returns the content assistant (completion) processors.
     *
     * @return the code assist processors
     */
    @Nullable
    public Map<String, CodeAssistProcessor> getContentAssistantProcessors();

    /**
     * Returns the reconciler.
     *
     * @return the reconciler
     */
    @Nullable
    public Reconciler getReconciler();

    /**
     * Returns the document partitioner.
     *
     * @return the document partitioner
     */
    @NotNull
    public DocumentPartitioner getPartitioner();

    /**
     * Return the document position model.
     *
     * @return the position model
     */
    @Nullable
    public DocumentPositionMap getDocumentPositionMap();

    /**
     * Return the annotation model.
     *
     * @return the annotation model
     */
    @Nullable
    public AnnotationModel getAnnotationModel();

    /**
     * Return the Quickassist assistant processor.
     *
     * @return the quickassist assistant processor
     */
    @Nullable
    public QuickAssistProcessor getQuickAssistProcessor();

    /**
     * Return the {@link org.eclipse.che.ide.jseditor.client.changeintercept.ChangeInterceptorProvider}.<br>
     * @return the change interceptors
     */
    @Nullable
    ChangeInterceptorProvider getChangeInterceptorProvider();
}
