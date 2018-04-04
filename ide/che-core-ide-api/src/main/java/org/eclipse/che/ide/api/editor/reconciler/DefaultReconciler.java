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
package org.eclipse.che.ide.api.editor.reconciler;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/**
 * Default implementation of {@link Reconciler}.
 *
 * @author Roman Nikitenko
 */
public class DefaultReconciler implements Reconciler {

  private final String partition;
  private final DocumentPartitioner partitioner;
  private final Map<String, ReconcilingStrategy> strategies;

  private DocumentHandle documentHandle;
  private TextEditor editor;

  @AssistedInject
  public DefaultReconciler(
      @Assisted final String partition, @Assisted final DocumentPartitioner partitioner) {
    this.partition = partition;
    this.partitioner = partitioner;
    strategies = new HashMap<>();
  }

  @Override
  public void install(TextEditor editor) {
    this.editor = editor;
    reconcilerDocumentChanged();
  }

  @Override
  public void uninstall() {
    strategies.values().forEach(ReconcilingStrategy::closeReconciler);
  }

  @Override
  public ReconcilingStrategy getReconcilingStrategy(final String contentType) {
    return strategies.get(contentType);
  }

  @Override
  public void addReconcilingStrategy(final String contentType, final ReconcilingStrategy strategy) {
    strategies.put(contentType, strategy);
  }

  @Override
  public String getDocumentPartitioning() {
    return partition;
  }

  @Override
  public void onDocumentChanged(final DocumentChangedEvent event) {}

  @Override
  public DocumentHandle getDocumentHandle() {
    return this.documentHandle;
  }

  @Override
  public void setDocumentHandle(final DocumentHandle handle) {
    this.documentHandle = handle;
  }

  /**
   * Returns the input document of the text view this reconciler is installed on.
   *
   * @return the reconciler document
   */
  protected Document getDocument() {
    return documentHandle.getDocument();
  }

  private void reconcilerDocumentChanged() {
    strategies
        .keySet()
        .forEach(
            key -> {
              ReconcilingStrategy reconcilingStrategy = strategies.get(key);
              reconcilingStrategy.setDocument(documentHandle.getDocument());
            });
  }
}
