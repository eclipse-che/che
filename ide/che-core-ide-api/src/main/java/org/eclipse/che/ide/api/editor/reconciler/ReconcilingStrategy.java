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
package org.eclipse.che.ide.api.editor.reconciler;

import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.Region;

/**
 * A reconciling strategy is used by an reconciler to reconcile a model based on text of a
 * particular content type.
 *
 * @author Evgen Vidolob
 */
public interface ReconcilingStrategy {
  /**
   * Tells this reconciling strategy on which document it will work. This method will be called
   * before any other method and can be called multiple times. The regions passed to the other
   * methods always refer to the most recent document passed into this method.
   *
   * @param document the document on which this strategy will work
   */
  void setDocument(Document document);

  /**
   * Activates incremental reconciling of the specified dirty region. As a dirty region might span
   * multiple content types, the segment of the dirty region which should be investigated is also
   * provided to this reconciling strategy. The given regions refer to the document passed into the
   * most recent call of {@link #setDocument(Document)}.
   *
   * @param dirtyRegion the document region which has been changed
   * @param subRegion the sub region in the dirty region which should be reconciled
   */
  void reconcile(DirtyRegion dirtyRegion, Region subRegion);

  /**
   * Activates non-incremental reconciling. The reconciling strategy is just told that there are
   * changes and that it should reconcile the given partition of the document most recently passed
   * into {@link #setDocument(Document)}.
   *
   * @param partition the document partition to be reconciled
   */
  void reconcile(Region partition);

  /**
   * This method should stop interact on changes in editor and clean up all outer reference (like
   * handling events and so on)
   */
  void closeReconciler();
}
