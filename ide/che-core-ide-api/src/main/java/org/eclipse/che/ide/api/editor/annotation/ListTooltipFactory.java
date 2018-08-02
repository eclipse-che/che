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
package org.eclipse.che.ide.api.editor.annotation;

import elemental.dom.Element;
import elemental.html.LIElement;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.Tooltip.Builder;
import org.eclipse.che.ide.ui.Tooltip.TooltipPositionerBuilder;
import org.eclipse.che.ide.ui.Tooltip.TooltipRenderer;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.ui.menu.PositionController.Positioner;
import org.eclipse.che.ide.ui.menu.PositionController.PositionerBuilder;
import org.eclipse.che.ide.util.dom.Elements;

/** Factory for a tooltip that shows list of messages. */
public final class ListTooltipFactory {

  private ListTooltipFactory() {}

  /** Static factory method for creating a list tooltip. */
  public static Tooltip create(
      final Element targetElement,
      final String header,
      final PositionController.VerticalAlign vAlign,
      final PositionController.HorizontalAlign hAlign,
      final String... tooltipText) {
    final PositionerBuilder positionrBuilder =
        new TooltipPositionerBuilder().setVerticalAlign(vAlign).setHorizontalAlign(hAlign);
    final Positioner positioner = positionrBuilder.buildAnchorPositioner(targetElement);
    final Builder builder = new Builder(targetElement, positioner);
    builder.setTooltipRenderer(new ListRenderer(header, tooltipText));

    return builder.build();
  }

  private static class ListRenderer implements TooltipRenderer {
    private final String header;
    private final String[] tooltipText;

    ListRenderer(final String header, final String... tooltipText) {
      this.tooltipText = tooltipText;
      this.header = header;
    }

    @Override
    public Element renderDom() {
      final Element content = Elements.createSpanElement();
      content.setInnerText(header);
      final Element list = Elements.createUListElement();
      for (final String tooltip : tooltipText) {
        final LIElement item = Elements.createLiElement();
        item.appendChild(Elements.createTextNode(tooltip));
        list.appendChild(item);
      }
      content.appendChild(list);
      return content;
    }
  }
}
