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
package org.eclipse.che.ide.api.editor.changeintercept;

import org.eclipse.che.ide.api.editor.document.ReadOnlyDocument;

/**
 * Interface for components that modify changes in the text.<br>
 * The interceptor should only modify the content using its return value (meaning it should not
 * directly access the odcument or editor to do changes).
 */
public interface TextChangeInterceptor {

  /**
   * Process a change in the editor text.
   *
   * @param change the incoming change
   * @param the read-only version of the document
   * @return the new version of the change (null doesn't modify the change)
   */
  TextChange processChange(TextChange change, ReadOnlyDocument document);
}
