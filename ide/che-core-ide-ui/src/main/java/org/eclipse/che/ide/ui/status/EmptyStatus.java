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
package org.eclipse.che.ide.ui.status;

import com.google.common.base.Predicate;
import com.google.gwt.user.client.ui.Widget;

/** Provide empty status message or widget for tree or other ui container like lists. */
public interface EmptyStatus<T extends Widget> {
  /** called when need to show empty status */
  void paint();

  /**
   * Initialize with parent widget and condition
   *
   * @param widget the widget that has empty state
   * @param showPredicate showing predicate
   */
  void init(T widget, Predicate<T> showPredicate);
}
