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
package org.eclipse.che.ide.processes;

import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.SpanElement;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.monitoring.MachineMonitors;
import org.eclipse.che.ide.terminal.AddTerminalClickHandler;
import org.eclipse.che.ide.terminal.HasAddTerminalClickHandler;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGImage;

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
public class MachineNodeRenderStrategy
    implements ProcessTreeNodeRenderStrategy,
        HasAddTerminalClickHandler,
        HasPreviewSshClickHandler {
  private final MachineResources resources;
  private final CoreLocalizationConstant locale;
  private final MachineMonitors machineMonitors;

  private AddTerminalClickHandler addTerminalClickHandler;
  private PreviewSshClickHandler previewSshClickHandler;

  @Inject
  public MachineNodeRenderStrategy(
      MachineResources resources,
      CoreLocalizationConstant locale,
      MachineMonitors machineMonitors) {
    this.resources = resources;
    this.locale = locale;
    this.machineMonitors = machineMonitors;
  }

  @Override
  public SpanElement renderSpanElementFor(ProcessTreeNode candidate) {
    return createMachineElement(candidate);
  }

  private SpanElement createMachineElement(final ProcessTreeNode node) {
    final String machineName = (String) node.getData();

    SpanElement root = Elements.createSpanElement();

    if (node.isTerminalServerRunning()) {
      SpanElement newTerminalButton =
          Elements.createSpanElement(resources.getCss().newTerminalButton());
      newTerminalButton.appendChild((Node) new SVGImage(resources.addTerminalIcon()).getElement());
      root.appendChild(newTerminalButton);

      Tooltip.create(newTerminalButton, BOTTOM, MIDDLE, locale.viewNewTerminalTooltip());

      newTerminalButton.addEventListener(
          Event.CLICK,
          event -> {
            event.stopPropagation();
            event.preventDefault();

            if (addTerminalClickHandler != null) {
              addTerminalClickHandler.onAddTerminalClick(machineName);
            }
          },
          true);

      EventListener blockMouseListener =
          event -> {
            event.stopPropagation();
            event.preventDefault();
          };

      newTerminalButton.addEventListener(Event.MOUSEDOWN, blockMouseListener, true);
      newTerminalButton.addEventListener(Event.MOUSEUP, blockMouseListener, true);
      newTerminalButton.addEventListener(Event.CLICK, blockMouseListener, true);
      newTerminalButton.addEventListener(Event.DBLCLICK, blockMouseListener, true);
    }

    if (node.isSshServerRunning()) {
      SpanElement sshButton = Elements.createSpanElement(resources.getCss().sshButton());
      sshButton.setTextContent("SSH");
      root.appendChild(sshButton);

      sshButton.addEventListener(
          Event.CLICK,
          event -> {
            if (previewSshClickHandler != null) {
              previewSshClickHandler.onPreviewSshClick(machineName);
            }
          },
          true);

      Tooltip.create(sshButton, BOTTOM, MIDDLE, locale.connectViaSSH());
    }

    Element monitorsElement = Elements.createSpanElement(resources.getCss().machineMonitors());
    root.appendChild(monitorsElement);

    Node monitorNode = (Node) machineMonitors.getMonitorWidget(machineName, this).getElement();
    monitorsElement.appendChild(monitorNode);

    Element nameElement = Elements.createSpanElement(resources.getCss().machineName());
    nameElement.setTextContent(machineName);
    Tooltip.create(nameElement, BOTTOM, MIDDLE, machineName);
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
