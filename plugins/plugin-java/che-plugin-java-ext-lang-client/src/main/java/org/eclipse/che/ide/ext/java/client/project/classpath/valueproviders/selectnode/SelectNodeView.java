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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode;

import com.google.inject.ImplementedBy;
import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.ClasspathNodeInterceptor;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Represents the structure of the workspace. It needs for choosing a needed node.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(SelectNodeViewImpl.class)
public interface SelectNodeView extends View<SelectNodeView.ActionDelegate> {
  /** Needs for delegate some function into SelectPath view. */
  interface ActionDelegate {
    /** Sets selected node. */
    void setSelectedNode(String path);
  }

  /**
   * Show structure of the tree.
   *
   * @param nodes list of the project root nodes
   * @param nodeInterceptor interceptor for filtering nodes
   */
  void setStructure(List<Node> nodes, ClasspathNodeInterceptor nodeInterceptor);

  /** Show dialog. */
  void showDialog();
}
