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
package org.eclipse.che.ide.command.explorer;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.node.CommandFileNode;
import org.eclipse.che.ide.command.node.CommandGoalNode;
import org.eclipse.che.ide.command.node.NodeFactory;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Implementation of {@link CommandsExplorerView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsExplorerViewImpl extends BaseView<CommandsExplorerView.ActionDelegate>
    implements CommandsExplorerView {

  private static final CommandsExplorerViewImplUiBinder UI_BINDER =
      GWT.create(CommandsExplorerViewImplUiBinder.class);

  private final CommandsTreeRenderer treeRenderer;
  private final NodeFactory nodeFactory;
  /** Mapping of the commands to the rendered tree nodes. */
  private final Map<CommandImpl, CommandFileNode> commandNodes;

  @UiField(provided = true)
  Tree tree;

  @Inject
  public CommandsExplorerViewImpl(
      ExplorerMessages messages, CommandResources resources, NodeFactory nodeFactory) {
    this.nodeFactory = nodeFactory;
    commandNodes = new HashMap<>();

    setTitle(messages.viewTitle());

    tree = new Tree(new NodeStorage(), new NodeLoader());
    tree.ensureDebugId("commands-explorer");

    treeRenderer = new CommandsTreeRenderer(tree.getTreeStyles(), resources, delegate);

    tree.setPresentationRenderer(treeRenderer);
    tree.getSelectionModel().setSelectionMode(SINGLE);

    tree.getSelectionModel()
        .addSelectionHandler(
            event -> {
              for (Node node : tree.getNodeStorage().getAll()) {
                final Element nodeContainerElement =
                    tree.getNodeDescriptor(node).getNodeContainerElement();

                if (nodeContainerElement != null) {
                  nodeContainerElement.removeAttribute("selected");
                }
              }

              tree.getNodeDescriptor(event.getSelectedItem())
                  .getNodeContainerElement()
                  .setAttribute("selected", "selected");
            });

    setContentWidget(UI_BINDER.createAndBindUi(this));
  }

  @Override
  protected void focusView() {
    tree.setFocus(true);
  }

  @Override
  public void setCommands(Map<CommandGoal, List<CommandImpl>> commands) {
    treeRenderer.setDelegate(delegate);

    renderCommands(commands);
  }

  private void renderCommands(Map<CommandGoal, List<CommandImpl>> commands) {
    commandNodes.clear();
    tree.getNodeStorage().clear();

    for (Entry<CommandGoal, List<CommandImpl>> entry : commands.entrySet()) {
      List<CommandFileNode> commandNodes = new ArrayList<>(entry.getValue().size());
      for (CommandImpl command : entry.getValue()) {
        final CommandFileNode commandFileNode = nodeFactory.newCommandFileNode(command);
        commandNodes.add(commandFileNode);

        this.commandNodes.put(command, commandFileNode);
      }

      final CommandGoalNode commandGoalNode =
          nodeFactory.newCommandGoalNode(entry.getKey(), commandNodes);
      tree.getNodeStorage().add(commandGoalNode);
    }

    tree.expandAll();
  }

  @Nullable
  @Override
  public CommandGoal getSelectedGoal() {
    final List<Node> selectedNodes = tree.getSelectionModel().getSelectedNodes();

    if (!selectedNodes.isEmpty()) {
      final Node selectedNode = selectedNodes.get(0);

      if (selectedNode instanceof CommandGoalNode) {
        return ((CommandGoalNode) selectedNode).getData();
      }
    }

    return null;
  }

  @Nullable
  @Override
  public CommandImpl getSelectedCommand() {
    final List<Node> selectedNodes = tree.getSelectionModel().getSelectedNodes();

    if (!selectedNodes.isEmpty()) {
      final Node selectedNode = selectedNodes.get(0);

      if (selectedNode instanceof CommandFileNode) {
        return ((CommandFileNode) selectedNode).getData();
      }
    }

    return null;
  }

  @Override
  public void selectCommand(CommandImpl command) {
    tree.getSelectionModel().setSelection(singletonList(commandNodes.get(command)));
  }

  interface CommandsExplorerViewImplUiBinder extends UiBinder<Widget, CommandsExplorerViewImpl> {}
}
