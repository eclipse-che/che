/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.ltk.core.refactoring.Change;

/**
 * Implementation for a refactoring preview change filter.
 *
 * @since 3.2
 */
public class RefactoringPreviewChangeFilter {

  /**
   * Is the specified change accepted by the filter?
   *
   * @param change the change to test
   * @return <code>true</code> if it is accepted for preview, <code>false</code> otherwise
   */
  public boolean select(final Change change) {
    return true;
  }
}
