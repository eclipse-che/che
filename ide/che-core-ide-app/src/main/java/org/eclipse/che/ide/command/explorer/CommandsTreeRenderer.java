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
package org.eclipse.che.ide.command.explorer;

import static com.google.gwt.user.client.Event.ONCLICK;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Event;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.explorer.CommandsExplorerView.ActionDelegate;
import org.eclipse.che.ide.command.node.CommandFileNode;
import org.eclipse.che.ide.command.node.CommandGoalNode;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Renderer for the commands tree.
 *
 * @author Artem Zatsarynnyi
 */
class CommandsTreeRenderer extends DefaultPresentationRenderer<Node> {

  private final CommandResources resources;

  private ActionDelegate delegate;

  CommandsTreeRenderer(TreeStyles treeStyles, CommandResources resources, ActionDelegate delegate) {
    super(treeStyles);

    this.resources = resources;
    this.delegate = delegate;
  }

  /** Sets the delegate that will handle events from the rendered DOM elements. */
  void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Element getPresentableTextContainer(Element content) {
    final Element presentableTextContainer = super.getPresentableTextContainer(content);
    presentableTextContainer.addClassName(resources.commandsExplorerCss().commandNodeText());

    return presentableTextContainer;
  }

  @Override
  public Element render(Node node, String domID, Tree.Joint joint, int depth) {
    final Element element = super.render(node, domID, joint, depth);
    final Element nodeContainerElement = element.getFirstChildElement();

    if (node instanceof CommandFileNode) {
      CommandFileNode commandNode = (CommandFileNode) node;

      nodeContainerElement.setId("command_" + commandNode.getDisplayName());

      renderCommandNode(commandNode, nodeContainerElement);
    } else if (node instanceof CommandGoalNode) {
      CommandGoalNode goalNode = (CommandGoalNode) node;

      nodeContainerElement.setId("goal_" + goalNode.getName());

      renderCommandGoalNode(nodeContainerElement);
    }

    return element;
  }

  private void renderCommandNode(CommandFileNode node, Element nodeContainerElement) {
    nodeContainerElement.addClassName(resources.commandsExplorerCss().commandNode());

    final Element removeCommandButton = createButton(FontAwesome.TRASH);
    Event.setEventListener(
        removeCommandButton,
        event -> {
          if (ONCLICK == event.getTypeInt()) {
            event.stopPropagation();
            delegate.onCommandRemove(node.getData());
          }
        });

    final Element duplicateCommandButton = createButton(resources.duplicateCommand());
    Event.setEventListener(
        duplicateCommandButton,
        event -> {
          if (ONCLICK == event.getTypeInt()) {
            event.stopPropagation();
            delegate.onCommandDuplicate(node.getData());
          }
        });

    final Element buttonsPanel = Document.get().createSpanElement();
    buttonsPanel.setClassName(resources.commandsExplorerCss().commandNodeButtonsPanel());
    buttonsPanel.appendChild(removeCommandButton);
    buttonsPanel.appendChild(duplicateCommandButton);

    nodeContainerElement.appendChild(buttonsPanel);

    removeCommandButton.setId("commands_tree-button-remove");
    duplicateCommandButton.setId("commands_tree-button-duplicate");
  }

  private void renderCommandGoalNode(Element nodeContainerElement) {
    nodeContainerElement.addClassName(resources.commandsExplorerCss().commandGoalNode());

    final Element addCommandButton = createButton(resources.addCommand());

    Event.setEventListener(
        addCommandButton,
        event -> {
          if (ONCLICK == event.getTypeInt()) {
            event.stopPropagation();
            delegate.onCommandAdd(
                addCommandButton.getAbsoluteLeft(), addCommandButton.getAbsoluteTop());
          }
        });

    nodeContainerElement.appendChild(addCommandButton);

    addCommandButton.setId("commands_tree-button-add");
  }

  private Element createButton(Object icon) {
    final Element button = Document.get().createSpanElement();
    button.appendChild(getIconElement(icon));

    Event.sinkEvents(button, ONCLICK);

    return button;
  }

  private Element getIconElement(Object icon) {
    if (icon instanceof SVGResource) {
      return ((SVGResource) icon).getSvg().getElement();
    } else if (icon instanceof String) {
      SpanElement element = Document.get().createSpanElement();
      element.getStyle().setFontSize(11., Style.Unit.PT);
      element.getStyle().setMarginTop(2., Style.Unit.PT);
      element.setInnerHTML((String) icon);
      return element;
    }

    throw new IllegalArgumentException("Icon type is undefined");
  }
}
