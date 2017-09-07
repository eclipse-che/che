/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.dom.Element;
import elemental.html.TableCellElement;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Renders breakpoint item the panel.
 *
 * @see Breakpoint
 * @author Anatolii Bazko
 */
public class BreakpointItemRender extends SimpleList.ListItemRenderer<Breakpoint> {

  private final DebuggerResources debuggerResources;

  public BreakpointItemRender(DebuggerResources debuggerResources) {
    this.debuggerResources = debuggerResources;
  }

  @Override
  public void render(Element itemElement, Breakpoint itemData) {
    TableCellElement label = Elements.createTDElement();

    SafeHtmlBuilder sb = new SafeHtmlBuilder();
    // Add icon
    sb.appendHtmlConstant("<table><tr><td>");
    SVGResource icon = debuggerResources.breakpoint();
    if (icon != null) {
      sb.appendHtmlConstant("<img src=\"" + icon.getSafeUri().asString() + "\">");
    }
    sb.appendHtmlConstant("</td>");

    // Add title
    sb.appendHtmlConstant("<td>");

    String path = itemData.getLocation().getTarget();
    sb.appendEscaped(
        path.substring(path.lastIndexOf("/") + 1)
            + ":"
            + String.valueOf(itemData.getLocation().getLineNumber()));
    sb.appendHtmlConstant("</td></tr></table>");

    label.setInnerHTML(sb.toSafeHtml().asString());

    itemElement.appendChild(label);
  }

  @Override
  public Element createElement() {
    return Elements.createTRElement();
  }
}
