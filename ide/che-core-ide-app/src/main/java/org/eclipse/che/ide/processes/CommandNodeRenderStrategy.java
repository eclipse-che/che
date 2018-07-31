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
import static org.eclipse.che.ide.util.dom.DomUtils.ensureDebugId;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.html.DivElement;
import elemental.html.SpanElement;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Strategy for rendering a command node.
 *
 * @author Vlad Zhukovskyi
 * @see ProcessTreeNodeRenderStrategy
 * @see HasStopProcessHandler
 * @since 5.11.0
 */
@Singleton
public class CommandNodeRenderStrategy
    implements ProcessTreeNodeRenderStrategy, HasStopProcessHandler {
  private final MachineResources resources;
  private final PartStackUIResources partResources;
  private final CoreLocalizationConstant locale;

  private StopProcessHandler stopProcessHandler;

  @Inject
  public CommandNodeRenderStrategy(
      MachineResources resources,
      PartStackUIResources partResources,
      CoreLocalizationConstant locale) {
    this.resources = resources;
    this.partResources = partResources;
    this.locale = locale;
  }

  @Override
  public SpanElement renderSpanElementFor(ProcessTreeNode candidate) {
    return createCommandElement(candidate);
  }

  private SpanElement createCommandElement(ProcessTreeNode node) {
    SpanElement root = Elements.createSpanElement(resources.getCss().commandTreeNode());

    root.appendChild(createCloseElement(node));
    root.appendChild(createStopProcessElement(node));

    SVGResource icon = node.getTitleIcon();
    if (icon != null) {
      SpanElement iconElement = Elements.createSpanElement(resources.getCss().processIcon());
      root.appendChild(iconElement);

      DivElement divElement = Elements.createDivElement(resources.getCss().processIconPanel());
      iconElement.appendChild(divElement);

      divElement.appendChild((Node) new SVGImage(icon).getElement());

      DivElement badgeElement = Elements.createDivElement(resources.getCss().processBadge());
      divElement.appendChild(badgeElement);
    }

    Element nameElement = Elements.createSpanElement();
    nameElement.setTextContent(node.getName());
    nameElement.setClassName(resources.getCss().processName());
    Tooltip.create(nameElement, BOTTOM, MIDDLE, node.getName());
    root.appendChild(nameElement);

    Element spanElement = Elements.createSpanElement();
    spanElement.setInnerHTML("&nbsp;");
    root.appendChild(spanElement);

    return root;
  }

  private SpanElement createStopProcessElement(final ProcessTreeNode node) {
    SpanElement stopProcessButton =
        Elements.createSpanElement(resources.getCss().processesPanelStopButtonForProcess());
    ensureDebugId(stopProcessButton, "stop-process-button-element");

    Tooltip.create(stopProcessButton, BOTTOM, MIDDLE, locale.viewStropProcessTooltip());

    stopProcessButton.addEventListener(
        Event.CLICK,
        event -> {
          if (stopProcessHandler != null) {
            stopProcessHandler.onStopProcessClick(node);
          }
        },
        true);

    return stopProcessButton;
  }

  private SpanElement createCloseElement(final ProcessTreeNode node) {
    SpanElement closeButton =
        Elements.createSpanElement(resources.getCss().processesPanelCloseButtonForProcess());
    ensureDebugId(closeButton, "close-command-node-button");

    SVGImage icon = new SVGImage(partResources.closeIcon());
    closeButton.appendChild((Node) icon.getElement());

    Tooltip.create(closeButton, BOTTOM, MIDDLE, locale.viewCloseProcessOutputTooltip());

    closeButton.addEventListener(
        Event.CLICK,
        event -> {
          if (stopProcessHandler != null) {
            stopProcessHandler.onCloseProcessOutputClick(node);
          }
        },
        true);

    return closeButton;
  }

  @Override
  public void addStopProcessHandler(StopProcessHandler handler) {
    stopProcessHandler = handler;
  }
}
