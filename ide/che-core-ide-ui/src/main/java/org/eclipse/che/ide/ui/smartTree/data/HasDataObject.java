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
