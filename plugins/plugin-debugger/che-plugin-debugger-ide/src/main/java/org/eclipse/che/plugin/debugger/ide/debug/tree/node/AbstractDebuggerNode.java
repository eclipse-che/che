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
package org.eclipse.che.plugin.debugger.ide.debug.tree.node;

import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.HasDataObject;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/**
 * Base tree node represents of the debugger variable data in the debugger tree.
 *
 * @author Alexander Andrienko
 */
public abstract class AbstractDebuggerNode<D> extends AbstractTreeNode
    implements HasDataObject<D>, HasPresentation {
  private NodePresentation nodePresentation;

  /** {@inheritDoc} */
  @Override
  public final NodePresentation getPresentation(boolean update) {
    if (nodePresentation == null) {
      nodePresentation = new NodePresentation();
      updatePresentation(nodePresentation);
    }

    if (update) {
      updatePresentation(nodePresentation);
    }
    return nodePresentation;
  }
}
