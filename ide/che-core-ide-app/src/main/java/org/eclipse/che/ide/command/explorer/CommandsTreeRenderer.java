/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.explorer;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.node.CommandFileNode;
import org.eclipse.che.ide.command.node.CommandGoalNode;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;
import org.vectomatic.dom.svg.ui.SVGResource;

import static com.google.gwt.user.client.Event.ONCLICK;

/**
 * Renderer for the commands tree.
 *
 * @author Artem Zatsarynnyi
 */
class CommandsTreeRenderer extends DefaultPresentationRenderer<Node> {

    private final CommandResources resources;

    private CommandsExplorerView.ActionDelegate delegate;

    CommandsTreeRenderer(TreeStyles treeStyles,
                         CommandResources resources,
                         CommandsExplorerView.ActionDelegate delegate) {
        super(treeStyles);

        this.resources = resources;
        this.delegate = delegate;
    }

    @Override
    public Element render(final Node node, String domID, Tree.Joint joint, int depth) {
        final Element element = super.render(node, domID, joint, depth);
        final Element nodeContainerElement = element.getFirstChildElement();

        if (node instanceof CommandFileNode) {

            nodeContainerElement.addClassName(resources.commandsExplorerCss().categorySubElementHeader());

            final SpanElement removeCommandButton = createButton(resources.removeCommand());
            Event.setEventListener(removeCommandButton, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    if (ONCLICK == event.getTypeInt()) {
                        event.stopPropagation();
                        delegate.onCommandRemove(((CommandFileNode)node).getData());
                    }
                }
            });

            final SpanElement duplicateCommandButton = createButton(resources.duplicateCommand());
            Event.setEventListener(duplicateCommandButton, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    if (ONCLICK == event.getTypeInt()) {
                        event.stopPropagation();
                        delegate.onCommandDuplicate(((CommandFileNode)node).getData());
                    }
                }
            });

            final SpanElement buttonsPanel = Document.get().createSpanElement();
            buttonsPanel.setClassName(resources.commandsExplorerCss().buttonArea());

            buttonsPanel.appendChild(removeCommandButton);
            buttonsPanel.appendChild(duplicateCommandButton);

            // add additional buttons to node container
            nodeContainerElement.appendChild(buttonsPanel);

        } else if (node instanceof CommandGoalNode) {

            nodeContainerElement.addClassName(resources.commandsExplorerCss().categoryHeader());

            final SpanElement addCommandButton = createButton(resources.addCommand());
            Event.setEventListener(addCommandButton, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    if (ONCLICK == event.getTypeInt()) {
                        event.stopPropagation();
                        delegate.onCommandAdd(addCommandButton.getAbsoluteLeft(), addCommandButton.getAbsoluteTop());
                    }
                }
            });

            nodeContainerElement.appendChild(addCommandButton);
        }

        return element;
    }

    private SpanElement createButton(SVGResource icon) {
        final SpanElement button = Document.get().createSpanElement();
        button.appendChild(icon.getSvg().getElement());

        Event.sinkEvents(button, ONCLICK);

        return button;
    }

    void setDelegate(CommandsExplorerView.ActionDelegate delegate) {
        this.delegate = delegate;
    }
}
