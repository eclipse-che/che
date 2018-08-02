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
package org.eclipse.che.ide.command.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.project.node.SyntheticNode;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGResource;

/** Tree node that represents {@link CommandGoal}. */
public class CommandGoalNode extends SyntheticNode<CommandGoal> {

  private final List<? extends AbstractCommandNode> commands;
  private final PromiseProvider promiseProvider;
  private final CommandUtils commandUtils;

  @Inject
  public CommandGoalNode(
      @Assisted CommandGoal data,
      @Assisted List<? extends AbstractCommandNode> commands,
      PromiseProvider promiseProvider,
      CommandUtils commandUtils) {
    super(data, null);

    this.commands = commands;
    this.promiseProvider = promiseProvider;
    this.commandUtils = commandUtils;
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    presentation.setPresentableText(getName().toUpperCase() + " (" + commands.size() + ")");
    presentation.setPresentableTextCss("font-weight: bold;");

    final SVGResource goalIcon = commandUtils.getCommandGoalIcon(getData().getId());
    if (goalIcon != null) {
      presentation.setPresentableIcon(goalIcon);
    }
  }

  @Override
  public String getName() {
    return getData().getId();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    List<Node> children = new ArrayList<>();
    children.addAll(commands);

    return promiseProvider.resolve(children);
  }
}
