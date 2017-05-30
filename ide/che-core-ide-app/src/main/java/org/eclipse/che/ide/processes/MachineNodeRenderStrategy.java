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
import elemental.html.SpanElement;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.monitoring.MachineMonitors;
import org.eclipse.che.ide.terminal.AddTerminalClickHandler;
import org.eclipse.che.ide.terminal.HasAddTerminalClickHandler;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGImage;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

/**
 * Strategy for rendering a machine node.
 *
 * @author Vlad Zhukovskyi
 * @see ProcessTreeNodeRenderStrategy
 * @see HasAddTerminalClickHandler
 * @see HasPreviewSshClickHandler
 * @since 5.11.0
 */
@Singleton
public class MachineNodeRenderStrategy implements ProcessTreeNodeRenderStrategy, HasAddTerminalClickHandler, HasPreviewSshClickHandler {
    private final MachineResources         resources;
    private final CoreLocalizationConstant locale;
    private final AppContext               appContext;
    private final MachineMonitors          machineMonitors;

    private AddTerminalClickHandler addTerminalClickHandler;
    private PreviewSshClickHandler  previewSshClickHandler;

    @Inject
    public MachineNodeRenderStrategy(MachineResources resources,
                                     CoreLocalizationConstant locale,
                                     AppContext appContext,
                                     MachineMonitors machineMonitors) {
        this.resources = resources;
        this.locale = locale;
        this.appContext = appContext;
        this.machineMonitors = machineMonitors;
    }

    @Override
    public SpanElement renderSpanElementFor(ProcessTreeNode candidate) {
        return createMachineElement(candidate);
    }

    private SpanElement createMachineElement(final ProcessTreeNode node) {
        final MachineEntity machine = (MachineEntity)node.getData();
        final String machineId = machine.getId();
        final MachineConfig machineConfig = machine.getConfig();

        SpanElement root = Elements.createSpanElement();

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

    @Override
    public void addAddTerminalClickHandler(AddTerminalClickHandler handler) {
        addTerminalClickHandler = handler;
    }

    @Override
    public void addPreviewSshClickHandler(PreviewSshClickHandler handler) {
        previewSshClickHandler = handler;
    }
}
