/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.editorconfig;

import static org.eclipse.che.ide.api.editor.partition.DocumentPartitioner.DEFAULT_CONTENT_TYPE;

import java.util.Map;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.changeintercept.ChangeInterceptorProvider;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.partition.ConstantPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.api.editor.reconciler.DefaultReconciler;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.signature.SignatureHelpProvider;

/** Default implementation of the {@link TextEditorConfiguration}. */
public class DefaultTextEditorConfiguration implements TextEditorConfiguration {

  private DefaultReconciler reconciler;
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
    return reconciler == null
        ? reconciler = new DefaultReconciler(DEFAULT_CONTENT_TYPE, getPartitioner())
        : reconciler;
  }

  @Override
  public DocumentPartitioner getPartitioner() {
    return partitioner == null ? partitioner = new ConstantPartitioner() : partitioner;
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
