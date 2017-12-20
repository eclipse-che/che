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

package org.eclipse.che.ide.ui.smartTree.presentation;

import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;

/**
 * Abstract class for utilize holding and creating {@link NodePresentation}, implements {@link
 * HasPresentation#getPresentation(boolean)}
 */
public abstract class AbstractPresentationNode extends AbstractTreeNode implements HasPresentation {

  private NodePresentation nodePresentation;

  @Override
  public NodePresentation getPresentation(boolean update) {
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
