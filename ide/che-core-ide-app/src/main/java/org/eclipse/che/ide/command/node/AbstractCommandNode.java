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
package org.eclipse.che.ide.command.node;

import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGResource;

/** Abstract tree node that represents {@link CommandImpl}. */
class AbstractCommandNode extends SyntheticNode<CommandImpl> {

  private final CommandUtils commandUtils;

  AbstractCommandNode(CommandImpl data, NodeSettings nodeSettings, CommandUtils commandUtils) {
    super(data, nodeSettings);

    this.commandUtils = commandUtils;
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    presentation.setPresentableText(getName());

    final SVGResource commandTypeIcon = commandUtils.getCommandTypeIcon(getData().getType());

    if (commandTypeIcon != null) {
      presentation.setPresentableIcon(commandTypeIcon);
    }
  }

  @Override
  public String getName() {
    return getData().getName();
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return null;
  }
}
