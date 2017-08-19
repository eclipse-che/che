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

import elemental.dom.Element;
import elemental.html.TableCellElement;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.util.dom.Elements;

import java.util.List;

/** @author Anatolii Bazko */
public class FrameItemRender extends SimpleList.ListItemRenderer<StackFrameDump> {

  @Override
  public void render(Element itemElement, StackFrameDump itemData) {
    TableCellElement label = Elements.createTDElement();

    SafeHtmlBuilder sb = new SafeHtmlBuilder();
    sb.appendEscaped(itemData.getLocation().getMethod().getName());
    sb.appendEscaped("(");

    List<? extends Variable> arguments = itemData.getLocation().getMethod().getArguments();
    for (int i = 0; i < arguments.size(); i++) {
      String type = arguments.get(i).getType();
      sb.appendEscaped(type.substring(type.lastIndexOf(".") + 1));

      if (i != arguments.size() - 1) {
        sb.appendEscaped(", ");
      }
    }

    sb.appendEscaped("):");
    sb.append(itemData.getLocation().getLineNumber());
    sb.appendEscaped(", ");

    String target = itemData.getLocation().getTarget();
    int classNameIndex = target.lastIndexOf(".");

    sb.appendEscaped(target.substring(classNameIndex + 1));
    sb.appendEscaped(" (");
    sb.appendEscaped(target.substring(0, classNameIndex));
    sb.appendEscaped(") ");

    label.setInnerHTML(sb.toSafeHtml().asString());
    itemElement.appendChild(label);
  }
}
