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
package org.eclipse.che.ide.part.editor.recent;

import java.util.List;

/**
 * Recent list of abstract items.
 *
 * @author Vlad Zhukovskiy
 */
public interface RecentList<T> {
  /**
   * Check whether recent list is empty.
   *
   * @return true if empty
   */
  boolean isEmpty();

  /**
   * Add abstract item to the recent list.
   *
   * @param item item to add
   * @return true if item has been added
   */
  boolean add(T item);

  /**
   * Remove abstract item from the recent list.
   *
   * @param item item to remove
   * @return true if item has been removed
   */
  boolean remove(T item);

  /**
   * Check if item contains in recent list.
   *
   * @param item item to check
   * @return true if contains
   */
  boolean contains(T item);

  /**
   * Get all recent list items.
   *
   * @return list of abstract items
   */
  List<T> getAll();

  /** Clear recent list. */
  void clear();
}
