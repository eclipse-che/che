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
package org.eclipse.che.ide.command.palette;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_DOWN;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_UP;
import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;
import static org.eclipse.che.ide.util.dom.Elements.createDivElement;
import static org.eclipse.che.ide.util.dom.Elements.createSpanElement;
import static org.eclipse.che.ide.util.dom.Elements.createTextNode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.html.DivElement;
import elemental.html.SpanElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.command.node.CommandGoalNode;
import org.eclipse.che.ide.command.node.ExecutableCommandNode;
import org.eclipse.che.ide.command.node.NodeFactory;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.window.Window;

/** Implementation of {@link CommandsPaletteView}. */
@Singleton
public class CommandsPaletteViewImpl extends Window implements CommandsPaletteView {

  private static final CommandsPaletteViewImplUiBinder UI_BINDER =
      GWT.create(CommandsPaletteViewImplUiBinder.class);

  private final NodeFactory nodeFactory;

  @UiField TextBox filterField;

  @UiField(provided = true)
  Tree tree;

  @UiField Label hintLabel;

  private ActionDelegate delegate;

  @Inject
  public CommandsPaletteViewImpl(NodeFactory nodeFactory, PaletteMessages messages) {
    this.nodeFactory = nodeFactory;

    tree = new Tree(new NodeStorage(), new NodeLoader());
    tree.getSelectionModel().setSelectionMode(SINGLE);

    setWidget(UI_BINDER.createAndBindUi(this));
    setTitle(messages.viewTitle());

    filterField.getElement().setAttribute("placeholder", messages.filterPlaceholder());
    initHintLabel();
    getFooter().removeFromParent();
  }

  private void initHintLabel() {
    final SpanElement upKeyLabel = createKeyLabel();
    upKeyLabel.setInnerHTML(FontAwesome.ARROW_UP);

    final SpanElement downKeyLabel = createKeyLabel();
    downKeyLabel.setInnerHTML(FontAwesome.ARROW_DOWN);

    final SpanElement enterKeyLabel = createKeyLabel();
    enterKeyLabel.getStyle().setPadding("0px 1px 1px 4px");
    enterKeyLabel.setInnerText(" Enter ");

    final DivElement hintElement = createDivElement();
    hintElement.appendChild(upKeyLabel);
    hintElement.appendChild(downKeyLabel);
    hintElement.appendChild(createTextNode(" to select and "));
    hintElement.appendChild(enterKeyLabel);
    hintElement.appendChild(createTextNode(" to execute"));

    hintLabel.getElement().appendChild((Element) hintElement);
  }

  /** Creates an html element for displaying keyboard key. */
  private SpanElement createKeyLabel() {
    SpanElement element = createSpanElement();

    element.getStyle().setFontWeight("bold");
    element.getStyle().setPadding("0 4px 1px 4px");
    element.getStyle().setMargin("0 3px");
    element.getStyle().setBorderWidth("1px");
    element.getStyle().setBorderStyle("solid");
    element.getStyle().setProperty("border-radius", "3px");

    return element;
  }

  @Override
  public void show() {
    super.show();

    filterField.setValue("");
    filterField.setFocus(true);
  }

  @Override
  public void close() {
    hide();
  }

  @Override
  public void setCommands(Map<CommandGoal, List<CommandImpl>> commands) {
    renderCommands(commands);
  }

  /** Render commands grouped by goals. */
  private void renderCommands(Map<CommandGoal, List<CommandImpl>> commands) {
    tree.getNodeStorage().clear();

    for (Entry<CommandGoal, List<CommandImpl>> entry : commands.entrySet()) {
      List<ExecutableCommandNode> commandNodes = new ArrayList<>(entry.getValue().size());

      for (final CommandImpl command : entry.getValue()) {
        commandNodes.add(
            nodeFactory.newExecutableCommandNode(
                command, () -> delegate.onCommandExecute(command)));
      }

      final CommandGoalNode commandGoalNode =
          nodeFactory.newCommandGoalNode(entry.getKey(), commandNodes);
      tree.getNodeStorage().add(commandGoalNode);
    }

    tree.expandAll();
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @UiHandler({"filterField"})
  void onFilterChanged(KeyUpEvent event) {
    switch (event.getNativeKeyCode()) {
      case KEY_UP:
        tree.getSelectionModel().selectPrevious();
        break;
      case KEY_DOWN:
        tree.getSelectionModel().selectNext();
        break;
      case KEY_ENTER:
        final List<Node> selectedNodes = tree.getSelectionModel().getSelectedNodes();

        if (!selectedNodes.isEmpty()) {
          final Node node = selectedNodes.get(0);

          if (node instanceof ExecutableCommandNode) {
            delegate.onCommandExecute(((ExecutableCommandNode) node).getData());
          } else if (node instanceof CommandGoalNode) {
            tree.setExpanded(node, !tree.isExpanded(node));
          }
        }
        break;
      default:
        delegate.onFilterChanged(filterField.getValue());
    }
  }

  interface CommandsPaletteViewImplUiBinder extends UiBinder<Widget, CommandsPaletteViewImpl> {}
}
