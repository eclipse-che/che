/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.theme.Style;

/** Tune {@link SplitLayoutPanel} splitter visibility */
@Singleton
public class SplitterFancyUtil {

  /** Improves splitter visibility. */
  public void tuneSplitter(SplitLayoutPanel splitLayoutPanel) {
    NodeList<Node> nodes = splitLayoutPanel.getElement().getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.getItem(i);
      if (node.hasChildNodes()) {
        com.google.gwt.dom.client.Element el = node.getFirstChild().cast();
        if ("gwt-SplitLayoutPanel-HDragger".equals(el.getClassName())) {
          tuneSplitter(el);
        }
      }
    }
  }

  /**
   * Tunes splitter. Makes it wider and adds double border to seem rich.
   *
   * @param el element to tune
   */
  private void tuneSplitter(Element el) {
    /** Add Z-Index to move the splitter on the top and make content visible */
    el.getParentElement().getStyle().setProperty("zIndex", "1000");
    el.getParentElement().getStyle().setProperty("overflow", "visible");

    /** Tune splitter catch panel */
    el.getStyle().setProperty("boxSizing", "border-box");
    el.getStyle().setProperty("width", "5px");
    el.getStyle().setProperty("overflow", "hidden");
    el.getStyle().setProperty("marginLeft", "-2px");
    el.getStyle().setProperty("backgroundColor", "transparent");

    /** Add small border */
    DivElement smallBorder = Document.get().createDivElement();
    smallBorder.getStyle().setProperty("position", "absolute");
    smallBorder.getStyle().setProperty("width", "1px");
    smallBorder.getStyle().setProperty("height", "100%");
    smallBorder.getStyle().setProperty("left", "2px");
    smallBorder.getStyle().setProperty("top", "0px");
    smallBorder.getStyle().setProperty("backgroundColor", Style.getSplitterSmallBorderColor());
    el.appendChild(smallBorder);
  }
}
