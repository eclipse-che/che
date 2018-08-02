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
package org.eclipse.che.ide.ext.git.client.merge;

import java.util.HashMap;
import java.util.List;
import org.eclipse.che.ide.ui.tree.NodeDataAdapter;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;

/**
 * The adapter for reference node.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public class ReferenceTreeNodeDataAdapter implements NodeDataAdapter<Reference> {
  private HashMap<Reference, TreeNodeElement<Reference>> treeNodeElements =
      new HashMap<Reference, TreeNodeElement<Reference>>();

  /** {@inheritDoc} */
  @Override
  public int compare(Reference a, Reference b) {
    return a.getDisplayName().compareTo(b.getDisplayName());
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasChildren(Reference data) {
    return data.getBranches() != null && !data.getBranches().isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public List<Reference> getChildren(Reference data) {
    return data.getBranches();
  }

  /** {@inheritDoc} */
  @Override
  public String getNodeId(Reference data) {
    return data.getFullName();
  }

  /** {@inheritDoc} */
  @Override
  public String getNodeName(Reference data) {
    return data.getDisplayName();
  }

  /** {@inheritDoc} */
  @Override
  public Reference getParent(Reference data) {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public TreeNodeElement<Reference> getRenderedTreeNode(Reference data) {
    return treeNodeElements.get(data);
  }

  /** {@inheritDoc} */
  @Override
  public void setNodeName(Reference data, String name) {
    // do nothing
  }

  /** {@inheritDoc} */
  @Override
  public void setRenderedTreeNode(Reference data, TreeNodeElement<Reference> renderedNode) {
    treeNodeElements.put(data, renderedNode);
  }

  /** {@inheritDoc} */
  @Override
  public Reference getDragDropTarget(Reference data) {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public List<String> getNodePath(Reference data) {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Reference getNodeByPath(Reference root, List<String> relativeNodePath) {
    return null;
  }
}
