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
package org.eclipse.che.ide.ext.java.client.command.mainclass;

import com.google.inject.ImplementedBy;
import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Represents the structure of the project for choosing Main class.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(SelectNodeViewImpl.class)
public interface SelectNodeView extends View<SelectNodeView.ActionDelegate> {
  /** Needs for delegate some function into SelectPath view. */
  interface ActionDelegate {
    /** Sets selected node. */
    void setSelectedNode(Resource resource, String fqn);
  }

  /**
   * Show structure of the tree.
   *
   * @param nodes list of the project root nodes
   */
  void setStructure(List<Node> nodes);

  /** Show dialog. */
  void showDialog();
}
