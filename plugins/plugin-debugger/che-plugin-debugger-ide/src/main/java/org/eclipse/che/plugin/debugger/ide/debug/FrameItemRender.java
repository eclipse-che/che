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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.dom.Element;
import elemental.html.TableCellElement;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Renders stack frame item the panel.
 *
 * @see StackFrameDump
 * @author Anatolii Bazko
 */
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

    Path path = Path.valueOf(itemData.getLocation().getTarget());

    String className;
    if (path.isAbsolute()) {
      className = path.removeFileExtension().lastSegment();
    } else {
      className = path.lastSegment();
    }

    sb.appendEscaped(className);

    label.setInnerHTML(sb.toSafeHtml().asString());
    itemElement.appendChild(label);
  }

  @Override
  public Element createElement() {
    return Elements.createTRElement();
  }
}
