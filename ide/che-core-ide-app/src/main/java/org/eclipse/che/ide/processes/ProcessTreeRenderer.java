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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.che.ide.util.dom.Elements.addClassName;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.dom.Element;
import elemental.html.SpanElement;
import java.util.Map;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.terminal.AddTerminalClickHandler;
import org.eclipse.che.ide.terminal.HasAddTerminalClickHandler;
import org.eclipse.che.ide.ui.tree.BaseNodeRenderer;

/**
 * Renderer for {@link ProcessTreeNode} UI presentation.
 *
 * @author Anna Shumilova
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ProcessTreeRenderer extends BaseNodeRenderer<ProcessTreeNode>
    implements HasAddTerminalClickHandler, HasPreviewSshClickHandler, HasStopProcessHandler {

  private final MachineResources resources;
  private final Map<String, ProcessTreeNodeRenderStrategy> renderStrategyMap;

  @Inject
  public ProcessTreeRenderer(
      MachineResources resources, Map<String, ProcessTreeNodeRenderStrategy> renderStrategyMap) {
    this.resources = resources;
    this.renderStrategyMap = renderStrategyMap;
  }

  @Override
  public Element getNodeKeyTextContainer(SpanElement treeNodeLabel) {
    return (Element) treeNodeLabel.getChildNodes().item(1);
  }

  @Override
  public SpanElement renderNodeContents(ProcessTreeNode node) {
    final ProcessTreeNodeRenderStrategy renderStrategy =
        renderStrategyMap.get(node.getType().getStringValue());

    checkNotNull(renderStrategy, "Unknown node type");

    final SpanElement spanElement = renderStrategy.renderSpanElementFor(node);

    addClassName(resources.getCss().processTreeNode(), spanElement);

    return spanElement;
  }

  @Override
  public void addStopProcessHandler(StopProcessHandler handler) {
    renderStrategyMap
        .values()
        .forEach(
            strategy -> {
              if (strategy instanceof HasStopProcessHandler) {
                ((HasStopProcessHandler) strategy).addStopProcessHandler(handler);
              }
            });
  }

  @Override
  public void addAddTerminalClickHandler(AddTerminalClickHandler handler) {
    renderStrategyMap
        .values()
        .forEach(
            strategy -> {
              if (strategy instanceof HasAddTerminalClickHandler) {
                ((HasAddTerminalClickHandler) strategy).addAddTerminalClickHandler(handler);
              }
            });
  }

  @Override
  public void addPreviewSshClickHandler(PreviewSshClickHandler handler) {
    renderStrategyMap
        .values()
        .forEach(
            strategy -> {
              if (strategy instanceof HasPreviewSshClickHandler) {
                ((HasPreviewSshClickHandler) strategy).addPreviewSshClickHandler(handler);
              }
            });
  }
}
