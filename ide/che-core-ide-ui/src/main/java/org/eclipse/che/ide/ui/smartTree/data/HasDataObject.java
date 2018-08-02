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
package org.eclipse.che.ide.ui.smartTree.data;

/**
 * Indicates that specified node can contains data object, e.g. project descriptor or item
 * reference.
 *
 * @author Vlad Zhukovskiy
 */
public interface HasDataObject<D> {
  /**
   * Retrieve stored data object.
   *
   * @return data object
   */
  D getData();

  /**
   * Store data object.
   *
   * @param data data object
   */
  void setData(D data);
}
