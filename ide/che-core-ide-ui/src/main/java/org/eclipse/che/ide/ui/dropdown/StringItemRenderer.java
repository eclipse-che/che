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
package org.eclipse.che.ide.ui.dropdown;

import static com.google.gwt.dom.client.Style.Unit.PX;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Renders the given {@link BaseListItem} as {@link Label}.
 *
 * @see BaseListItem
 */
public class StringItemRenderer implements DropdownListItemRenderer {

  private final BaseListItem<String> item;

  private Widget headerWidget;
  private Widget listWidget;

  public StringItemRenderer(BaseListItem<String> item) {
    this.item = item;
  }

  @Override
  public Widget renderHeaderWidget() {
    if (headerWidget == null) {
      headerWidget = new Label(item.getValue());
    }

    return headerWidget;
  }

  @Override
  public Widget renderListWidget() {
    if (listWidget == null) {
      listWidget = new Label(item.getValue());
      listWidget.getElement().getStyle().setMarginBottom(0, PX);
    }

    return listWidget;
  }
}
