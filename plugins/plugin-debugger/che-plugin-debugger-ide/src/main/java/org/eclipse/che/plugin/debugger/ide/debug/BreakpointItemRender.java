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
package org.eclipse.che.plugin.debugger.ide.debug;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.dom.Element;
import elemental.html.TableCellElement;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.ide.debug.BreakpointResources;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Renders breakpoint item the panel.
 *
 * @see Breakpoint
 * @author Anatolii Bazko
 */
public class BreakpointItemRender
    extends SimpleList.ListItemRenderer<DebuggerView.ActiveBreakpointWrapper> {

  private final BreakpointResources breakpointResources;

  public BreakpointItemRender(BreakpointResources breakpointResources) {
    this.breakpointResources = breakpointResources;
  }

  @Override
  public void render(Element itemElement, DebuggerView.ActiveBreakpointWrapper breakpointWrapper) {
    Breakpoint breakpoint = breakpointWrapper.getBreakpoint();
    BreakpointConfiguration conf = breakpoint.getBreakpointConfiguration();
    BreakpointResources.Css css = breakpointResources.getCss();

    TableCellElement label = Elements.createTDElement();

    SafeHtmlBuilder sb = new SafeHtmlBuilder();
    // Add icon
    sb.appendHtmlConstant("<table><tr><td>");
    sb.appendHtmlConstant("<div class=\"");
    if (!breakpoint.isEnabled()) {
      sb.appendHtmlConstant(css.breakpoint() + " " + css.disabled());
    } else if (breakpointWrapper.isActive()) {
      sb.appendHtmlConstant(css.breakpoint() + " " + css.active());
    } else {
      sb.appendHtmlConstant(css.breakpoint() + " " + css.inactive());
    }
    sb.appendHtmlConstant("\" style=\"height: 14px; width: 14px; text-align: center\"");
    sb.appendHtmlConstant(
        " id=\""
            + breakpoint.getLocation().getTarget()
            + ":"
            + breakpoint.getLocation().getLineNumber()
            + "\">");

    boolean hasCondition =
        conf != null
            && ((conf.isConditionEnabled() && !isNullOrEmpty(conf.getCondition()))
                || (conf.isHitCountEnabled() && conf.getHitCount() != 0));
    if (hasCondition) {
      sb.appendHtmlConstant("?");
    }
    sb.appendHtmlConstant("</div>");

    sb.appendHtmlConstant("</td>");

    // Add title
    sb.appendHtmlConstant("<td>");

    String path = breakpoint.getLocation().getTarget();
    sb.appendEscaped(
        path.substring(path.lastIndexOf("/") + 1)
            + ":"
            + String.valueOf(breakpoint.getLocation().getLineNumber()));
    sb.appendHtmlConstant("</td></tr></table>");

    label.setInnerHTML(sb.toSafeHtml().asString());

    itemElement.appendChild(label);
  }

  @Override
  public Element createElement() {
    return Elements.createTRElement();
  }
}
