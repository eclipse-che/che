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
package org.eclipse.che.ide.extension.machine.client.processes;

import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.DivElement;
import elemental.html.SpanElement;

import com.google.inject.Inject;

import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.ui.Tooltip;

import static org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter.SSH_PORT;
import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;
import org.eclipse.che.ide.ui.tree.NodeRenderer;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Renderer for {@ProcessTreeNode} UI presentation.
 *
 * @author Anna Shumilova
 * @author Roman Nikitenko
 */
public class ProcessTreeRenderer implements NodeRenderer<ProcessTreeNode> {

    private final MachineResources            resources;
    private final MachineLocalizationConstant locale;
    private final PartStackUIResources        partStackUIResources;
    private       AddTerminalClickHandler     addTerminalClickHandler;
    private       PreviewSshClickHandler      previewSshClickHandler;
    private       StopProcessHandler          stopProcessHandler;

    @Inject
    public ProcessTreeRenderer(MachineResources resources, MachineLocalizationConstant locale,
                               PartStackUIResources partStackUIResources) {
        this.resources = resources;
        this.locale = locale;
        this.partStackUIResources = partStackUIResources;
    }

    @Override
    public Element getNodeKeyTextContainer(SpanElement treeNodeLabel) {
        return (Element)treeNodeLabel.getChildNodes().item(1);
    }

    @Override
    public SpanElement renderNodeContents(ProcessTreeNode node) {
        SpanElement treeNode;
        switch (node.getType()) {
            case MACHINE_NODE:
                treeNode = createMachineElement(node, (MachineDto)node.getData());
                break;
            case COMMAND_NODE:
                treeNode = createCommandElement(node);
                break;
            case TERMINAL_NODE:
                treeNode = createTerminalElement(node);
                break;
            default:
                treeNode = Elements.createSpanElement();
        }

        Elements.addClassName(resources.getCss().processTreeNode(), treeNode);
        return treeNode;
    }

    private SpanElement createMachineElement(final ProcessTreeNode node, final MachineDto machine) {
        SpanElement root = Elements.createSpanElement();
        if (machine.getConfig().isDev()) {
            SpanElement devLabel = Elements.createSpanElement(resources.getCss().devMachineLabel());
            devLabel.setTextContent(locale.viewProcessesDevTitle());
            root.appendChild(devLabel);
        }

        Element statusElement = Elements.createSpanElement(resources.getCss().machineStatus());
        root.appendChild(statusElement);

        if (node.isRunning()) {
            statusElement.appendChild(Elements.createDivElement(resources.getCss().machineStatusRunning()));
        } else {
            statusElement.appendChild(Elements.createDivElement(resources.getCss().machineStatusPausedLeft()));
            statusElement.appendChild(Elements.createDivElement(resources.getCss().machineStatusPausedRight()));
        }

        Tooltip.create((elemental.dom.Element) statusElement,
                BOTTOM,
                MIDDLE,
                locale.viewMachineRunningTooltip());

        SpanElement newTerminalButton = Elements.createSpanElement(resources.getCss().processButton());
        newTerminalButton.setTextContent("+");
        root.appendChild(newTerminalButton);

        Tooltip.create((elemental.dom.Element)newTerminalButton,
                       BOTTOM,
                       MIDDLE,
                       locale.viewNewTerminalTooltip());

        if (machine.getRuntime().getServers().containsKey(SSH_PORT)) {
            SpanElement sshButton = Elements.createSpanElement(resources.getCss().sshButton());
            sshButton.setTextContent("SSH");
            root.appendChild(sshButton);

            sshButton.addEventListener(Event.CLICK, new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    if (previewSshClickHandler != null) {
                        previewSshClickHandler.onPreviewSshClick(machine.getId());
                    }
                }
            }, true);
        }

        newTerminalButton.addEventListener(Event.CLICK, new EventListener() {
            @Override
            public void handleEvent(Event event) {
                event.stopPropagation();
                event.preventDefault();

                if (addTerminalClickHandler != null) {
                    addTerminalClickHandler.onAddTerminalClick(machine.getId());
                }
            }
        }, true);

        /**
         * This listener cancels mouse events on '+' button and prevents the jitter of the selection in the tree.
         */
        EventListener blockMouseListener = new EventListener() {
            @Override
            public void handleEvent(Event event) {
                event.stopPropagation();
                event.preventDefault();
            }
        };

        /**
         * Prevent jitter when pressing mouse on '+' button.
         */
        newTerminalButton.addEventListener(Event.MOUSEDOWN, blockMouseListener, true);
        newTerminalButton.addEventListener(Event.MOUSEUP, blockMouseListener, true);
        newTerminalButton.addEventListener(Event.CLICK, blockMouseListener, true);
        newTerminalButton.addEventListener(Event.DBLCLICK, blockMouseListener, true);


        Element nameElement = Elements.createSpanElement(resources.getCss().machineLabel());
        nameElement.setTextContent(machine.getConfig().getName());
        root.appendChild(nameElement);

        return root;
    }

    private SpanElement createCommandElement(ProcessTreeNode node) {
        SpanElement root = Elements.createSpanElement();
        root.setAttribute("running", "" + node.isRunning());

        root.appendChild(createCloseElement(node));
        root.appendChild(createStopProcessElement(node));

        SVGResource icon = node.getTitleIcon();
        if (icon != null) {
            SpanElement iconElement = Elements.createSpanElement(resources.getCss().processIcon());
            root.appendChild(iconElement);

            DivElement divElement = Elements.createDivElement(resources.getCss().processIconPanel());
            iconElement.appendChild(divElement);

            divElement.appendChild((Node)new SVGImage(icon).getElement());

            DivElement badgeElement = Elements.createDivElement(resources.getCss().processBadge());
            divElement.appendChild(badgeElement);
        }

        Element nameElement = Elements.createSpanElement();
        nameElement.setTextContent(node.getName());
        root.appendChild(nameElement);

        Element spanElement = Elements.createSpanElement();
        spanElement.setInnerHTML("&nbsp;");
        root.appendChild(spanElement);

        return root;
    }

    private SpanElement createTerminalElement(ProcessTreeNode node) {
        SpanElement root = Elements.createSpanElement();

        SVGResource icon = node.getTitleIcon();
        if (icon != null) {
            SpanElement iconElement = Elements.createSpanElement(resources.getCss().processIcon());
            root.appendChild(iconElement);

            DivElement divElement = Elements.createDivElement(resources.getCss().processIconPanel());
            iconElement.appendChild(divElement);

            divElement.appendChild((Node) new SVGImage(icon).getElement());
        }

        root.appendChild(createCloseElement(node));

        Element nameElement = Elements.createSpanElement();
        nameElement.setTextContent(node.getName());
        root.appendChild(nameElement);

        Element spanElement = Elements.createSpanElement();
        spanElement.setInnerHTML("&nbsp;");
        root.appendChild(spanElement);

        return root;
    }

    private SpanElement createCloseElement(final ProcessTreeNode node) {
        SpanElement closeButton = Elements.createSpanElement(resources.getCss().processesPanelCloseButtonForProcess());

        SVGImage icon = new SVGImage(partStackUIResources.closeIcon());
        closeButton.appendChild((Node) icon.getElement());

        Tooltip.create((elemental.dom.Element)closeButton,
                       BOTTOM,
                       MIDDLE,
                       locale.viewCloseProcessOutputTooltip());

        closeButton.addEventListener(Event.CLICK, new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (stopProcessHandler != null) {
                    stopProcessHandler.onCloseProcessOutputClick(node);
                }
            }
        }, true);

        return closeButton;
    }

    private SpanElement createStopProcessElement(final ProcessTreeNode node) {
        SpanElement stopProcessButton = Elements.createSpanElement(resources.getCss().processesPanelStopButtonForProcess());

        Tooltip.create((elemental.dom.Element) stopProcessButton,
                BOTTOM,
                MIDDLE,
                locale.viewStropProcessTooltip());

        stopProcessButton.addEventListener(Event.CLICK, new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (stopProcessHandler != null) {
                    stopProcessHandler.onStopProcessClick(node);
                }
            }
        }, true);

        return stopProcessButton;
    }


    @Override
    public void updateNodeContents(TreeNodeElement<ProcessTreeNode> treeNode) {
    }

    public void setAddTerminalClickHandler(AddTerminalClickHandler addTerminalClickHandler) {
        this.addTerminalClickHandler = addTerminalClickHandler;
    }

    public void setPreviewSshClickHandler(PreviewSshClickHandler previewSshClickHandler) {
        this.previewSshClickHandler = previewSshClickHandler;
    }

    public void setStopProcessHandler(StopProcessHandler stopProcessHandler) {
        this.stopProcessHandler = stopProcessHandler;
    }

}
