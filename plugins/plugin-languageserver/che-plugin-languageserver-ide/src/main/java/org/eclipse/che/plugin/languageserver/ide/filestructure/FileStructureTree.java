/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.filestructure;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import org.eclipse.che.ide.ui.smartTree.NodeDescriptor;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;

/**
 * A subclass that allows to open inner nodes.
 *
 * @author Thomas Mäder
 */
public class FileStructureTree extends Tree {

  public FileStructureTree(NodeStorage nodeStorage, NodeLoader nodeLoader) {
    super(nodeStorage, nodeLoader);
  }

  public void onBrowserEvent(Event event) {
    switch (event.getTypeInt()) {
      case Event.ONDBLCLICK:
        onDoubleClick(event);
        break;
      default:
        super.onBrowserEvent(event);
    }
  }

  private void onDoubleClick(Event event) {
    NodeDescriptor nodeDescriptor = getNodeDescriptor(event.getEventTarget().<Element>cast());
    if (nodeDescriptor == null) {
      return;
    }

    if (nodeDescriptor.getNode() instanceof HasAction) {
      ((HasAction) nodeDescriptor.getNode()).actionPerformed();
    } else {
      toggle(nodeDescriptor.getNode());
    }
  }
}
