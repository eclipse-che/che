/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
