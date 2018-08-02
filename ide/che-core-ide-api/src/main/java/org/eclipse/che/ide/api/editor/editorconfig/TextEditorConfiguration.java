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
package org.eclipse.che.ide.api.editor.editorconfig;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.changeintercept.ChangeInterceptorProvider;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistProcessor;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.signature.SignatureHelpProvider;

/** Configure extended functions of the editor. */
public interface TextEditorConfiguration {

  /**
   * Returns the visual width of the tab character. This implementation always returns 3.
   *
   * @return the tab width
   */
  int getTabWidth();

  /**
   * Returns the content formatter.
   *
   * @return the content formatter
   */
  @Nullable
  ContentFormatter getContentFormatter();

  /**
   * Returns the content assistant (completion) processors.
   *
   * @return the code assist processors
   */
  @Nullable
  Map<String, CodeAssistProcessor> getContentAssistantProcessors();

  /**
   * Returns the reconciler.
   *
   * @return the reconciler
   */
  @Nullable
  Reconciler getReconciler();

  /**
   * Returns the document partitioner.
   *
   * @return the document partitioner
   */
  @NotNull
  DocumentPartitioner getPartitioner();

  /**
   * Return the document position model.
   *
   * @return the position model
   */
  @Nullable
  DocumentPositionMap getDocumentPositionMap();

  /**
   * Return the annotation model.
   *
   * @return the annotation model
   */
  @Nullable
  AnnotationModel getAnnotationModel();

  /**
   * Return the Quickassist assistant processor.
   *
   * @return the quickassist assistant processor
   */
  @Nullable
  QuickAssistProcessor getQuickAssistProcessor();

  /**
   * Return the {@link ChangeInterceptorProvider}.<br>
   *
   * @return the change interceptors
   */
  @Nullable
  ChangeInterceptorProvider getChangeInterceptorProvider();

  /**
   * Return the {@link SignatureHelpProvider}
   *
   * @return the signature help provider
   */
  @Nullable
  SignatureHelpProvider getSignatureHelpProvider();
}
