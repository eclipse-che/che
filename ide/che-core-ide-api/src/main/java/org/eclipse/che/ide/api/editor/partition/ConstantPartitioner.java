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
package org.eclipse.che.ide.api.editor.partition;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.text.TypedRegion;
import org.eclipse.che.ide.api.editor.text.TypedRegionImpl;

/** A no-op partitioner which partitions the document in one single default type partititon. */
public class ConstantPartitioner implements DocumentPartitioner {

  /** The list with a single content type, the default one. */
  private final List<String> legalContentTypes;

  /** The single content type. */
  private final String contentType;

  /** The full document length. */
  private int documentLength = 0;

  /** Handle on the document. */
  private DocumentHandle documentHandle;

  /**
   * Constructor for a {@link ConstantPartitioner} that has a single partition of type ContentType.
   *
   * @param contentType the single content type
   */
  public ConstantPartitioner(final String contentType) {
    this.contentType = contentType;
    this.legalContentTypes = Collections.singletonList(this.contentType);
  }

  /**
   * Constructor for a {@link ConstantPartitioner} that has a single partition of type {@link
   * DefaultPartitioner#DEFAULT_CONTENT_TYPE}.
   */
  public ConstantPartitioner() {
    this(DEFAULT_CONTENT_TYPE);
  }

  @Override
  public void onDocumentChanged(final DocumentChangedEvent event) {
    final int removed = event.getLength();
    int added = 0;
    if (event.getText() != null) {
      added = event.getText().length();
    }
    final int sizeDelta = added - removed;
    this.documentLength += sizeDelta;
  }

  @Override
  public void initialize() {
    this.documentLength = getDocumentHandle().getDocument().getContentsCharCount();
  }

  @Override
  public List<String> getLegalContentTypes() {
    return legalContentTypes;
  }

  @Override
  public String getContentType(final int offset) {
    return this.contentType;
  }

  @Override
  public List<TypedRegion> computePartitioning(final int offset, final int length) {
    final TypedRegion region = getPartition(offset);
    return Collections.singletonList(region);
  }

  @Override
  public TypedRegion getPartition(final int offset) {
    return new TypedRegionImpl(offset, this.documentLength, this.contentType);
  }

  @Override
  public DocumentHandle getDocumentHandle() {
    return documentHandle;
  }

  @Override
  public void setDocumentHandle(DocumentHandle handle) {
    this.documentHandle = handle;
  }

  @Override
  public void release() {}
}
