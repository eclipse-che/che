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
package org.eclipse.che.ide.api.editor.codeassist;

import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.LinearRange;

/** Interface for completion objects. */
public interface Completion {

  /**
   * Inserts the proposed completion into the given document.
   *
   * @param document the document into which to insert the proposed completion
   */
  void apply(Document document);

  /**
   * Returns the new selection after the proposal has been applied to the given document in absolute
   * document coordinates. If it returns <code>null</code>, no new selection is set.
   *
   * <p>A document change can trigger other document changes, which have to be taken into account
   * when calculating the new selection. Typically, this would be done by installing a document
   * listener or by using a document position during {@link #apply(Document)}.
   *
   * @param document the document into which the proposed completion has been inserted
   * @return the new selection in absolute document coordinates
   */
  LinearRange getSelection(Document document);
}
