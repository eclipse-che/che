/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.processes;

import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.DivElement;
import elemental.html.SpanElement;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.monitoring.MachineMonitors;
import org.eclipse.che.ide.terminal.AddTerminalClickHandler;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.tree.NodeRenderer;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;
import static org.eclipse.che.ide.util.dom.DomUtils.ensureDebugId;

/**
 * Renderer for {@link ProcessTreeNode} UI presentation.
 *
 * @author Anna Shumilova
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 */
public class ProcessTreeRenderer implements NodeRenderer<ProcessTreeNode> {

    private final MachineResources         resources;
    private final CoreLocalizationConstant locale;
    private final PartStackUIResources     partStackUIResources;
    private final MachineMonitors          machineMonitors;
    private final AppContext               appContext;

    private AddTerminalClickHandler addTerminalClickHandler;
    private PreviewSshClickHandler  previewSshClickHandler;
    private StopProcessHandler      stopProcessHandler;

    @Inject
    public ProcessTreeRenderer(MachineResources resources,
                               CoreLocalizationConstant locale,
                               PartStackUIResources partStackUIResources,
                               MachineMonitors machineMonitors,
                               AppContext appContext) {
        this.resources = resources;
        this.locale = locale;
        this.partStackUIResources = partStackUIResources;
        this.machineMonitors = machineMonitors;
        this.appContext = appContext;
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
                treeNode = createMachineElement(node);
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

    private SpanElement createMachineElement(final ProcessTreeNode node) {
        final MachineEntity machine = (MachineEntity)node.getData();
        final String machineId = machine.getId();
        final MachineConfig machineConfig = machine.getConfig();

        SpanElement root = Elements.createSpanElement();

        Element statusElement = Elements.createSpanElement(resources.getCss().machineStatus());
        root.appendChild(statusElement);

        if (node.isRunning()) {
            statusElement.appendChild(Elements.createDivElement(resources.getCss().machineStatusRunning()));
        } else {
            statusElement.appendChild(Elements.createDivElement(resources.getCss().machineStatusPausedLeft()));
            statusElement.appendChild(Elements.createDivElement(resources.getCss().machineStatusPausedRight()));
        }

        Tooltip.create(statusElement, BOTTOM, MIDDLE, locale.viewMachineRunningTooltip());

        Workspace workspace = appContext.getWorkspace();
        if (workspace != null && RUNNING == workspace.getStatus() && node.hasTerminalAgent()) {
            SpanElement newTerminalButton = Elements.createSpanElement(resources.getCss().newTerminalButton());
            newTerminalButton.appendChild((Node)new SVGImage(resources.addTerminalIcon()).getElement());
            root.appendChild(newTerminalButton);

            Tooltip.create(newTerminalButton, BOTTOM, MIDDLE, locale.viewNewTerminalTooltip());

            newTerminalButton.addEventListener(Event.CLICK, event -> {
                event.stopPropagation();
                event.preventDefault();

                if (addTerminalClickHandler != null) {
                    addTerminalClickHandler.onAddTerminalClick(machineId);
                }
            }, true);

            EventListener blockMouseListener = event -> {
                event.stopPropagation();
                event.preventDefault();
            };

            newTerminalButton.addEventListener(Event.MOUSEDOWN, blockMouseListener, true);
            newTerminalButton.addEventListener(Event.MOUSEUP, blockMouseListener, true);
            newTerminalButton.addEventListener(Event.CLICK, blockMouseListener, true);
            newTerminalButton.addEventListener(Event.DBLCLICK, blockMouseListener, true);
        }

        if (node.isRunning() && node.hasSSHAgent()) {
            SpanElement sshButton = Elements.createSpanElement(resources.getCss().sshButton());
            sshButton.setTextContent("SSH");
            root.appendChild(sshButton);

            sshButton.addEventListener(Event.CLICK, event -> {
                if (previewSshClickHandler != null) {
                    previewSshClickHandler.onPreviewSshClick(machineId);
                }
            }, true);

            Tooltip.create(sshButton, BOTTOM, MIDDLE, locale.connectViaSSH());
        }

        Element monitorsElement = Elements.createSpanElement(resources.getCss().machineMonitors());
        root.appendChild(monitorsElement);

        Node monitorNode = (Node)machineMonitors.getMonitorWidget(machineId, this).getElement();
        monitorsElement.appendChild(monitorNode);

        Element nameElement = Elements.createSpanElement(resources.getCss().nameLabel());
        nameElement.setTextContent(machineConfig.getName());
        Tooltip.create(nameElement, BOTTOM, MIDDLE, machineConfig.getName());
        root.appendChild(nameElement);

        return root;
    }

    private SpanElement createCommandElement(ProcessTreeNode node) {
        SpanElement root = Elements.createSpanElement(resources.getCss().commandTreeNode());
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
        Tooltip.create(nameElement,
                       BOTTOM,
                       MIDDLE,
                       node.getName());
        root.appendChild(nameElement);

        Element spanElement = Elements.createSpanElement();
        spanElement.setInnerHTML("&nbsp;");
        root.appendChild(spanElement);

        return root;
    }

    private SpanElement createTerminalElement(ProcessTreeNode node) {
        SpanElement root = Elements.createSpanElement(resources.getCss().commandTreeNode());
        ensureDebugId(root, "terminal-root-element");

        root.appendChild(createCloseElement(node));

        SVGResource icon = node.getTitleIcon();
        if (icon != null) {
            SpanElement iconElement = Elements.createSpanElement(resources.getCss().processIcon());
            ensureDebugId(iconElement, "terminal-icon-element");

            root.appendChild(iconElement);

            DivElement divElement = Elements.createDivElement(resources.getCss().processIconPanel());
            iconElement.appendChild(divElement);

            divElement.appendChild((Node)new SVGImage(icon).getElement());
        }

        Element nameElement = Elements.createSpanElement();
        nameElement.setTextContent(node.getName());
        ensureDebugId(nameElement, "terminal-name-element");

        Tooltip.create(nameElement, BOTTOM, MIDDLE, node.getName());
        root.appendChild(nameElement);

        Element spanElement = Elements.createSpanElement();
        spanElement.setInnerHTML("&nbsp;");
        root.appendChild(spanElement);

        return root;
    }

    private SpanElement createCloseElement(final ProcessTreeNode node) {
        SpanElement closeButton = Elements.createSpanElement(resources.getCss().processesPanelCloseButtonForProcess());
        ensureDebugId(closeButton, "close-button");

        SVGImage icon = new SVGImage(partStackUIResources.closeIcon());
        closeButton.appendChild((Node)icon.getElement());

        Tooltip.create(closeButton, BOTTOM, MIDDLE, locale.viewCloseProcessOutputTooltip());

        closeButton.addEventListener(Event.CLICK, event -> {
            if (stopProcessHandler != null) {
                stopProcessHandler.onCloseProcessOutputClick(node);
            }
        }, true);

        return closeButton;
    }

    private SpanElement createStopProcessElement(final ProcessTreeNode node) {
        SpanElement stopProcessButton = Elements.createSpanElement(resources.getCss().processesPanelStopButtonForProcess());
        ensureDebugId(stopProcessButton, "stop-process-button-element");

        Tooltip.create(stopProcessButton, BOTTOM, MIDDLE, locale.viewStropProcessTooltip());

        stopProcessButton.addEventListener(Event.CLICK, event -> {
            if (stopProcessHandler != null) {
                stopProcessHandler.onStopProcessClick(node);
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
