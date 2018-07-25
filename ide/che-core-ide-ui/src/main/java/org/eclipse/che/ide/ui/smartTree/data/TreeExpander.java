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
package org.eclipse.che.ide.ui.smartTree.data;

import com.google.common.annotations.Beta;

/**
 * Component which performs basic tree operation such as expand and collapse.
 *
 * @author Vlad Zhukovskyi
 * @since 5.0.0
 */
@Beta
public interface TreeExpander {

  /** Perform tree expand in case if {@link #isExpandEnabled()} returns {@code true}. */
  void expandTree();

  /**
   * Returns {@code true} in case if tree expand is possible.
   *
   * @return {@code true} in case if tree expand is possible, otherwise {@code false}
   */
  boolean isExpandEnabled();

  /** Perform tree collapse in case if {@link #isCollapseEnabled()} returns {@code true}. */
  void collapseTree();

  /**
   * Returns {@code true} in case if tree collapse is possible.
   *
   * @return {@code true} in case if tree collapse is possible, otherwise {@code false}
   */
  boolean isCollapseEnabled();
}
