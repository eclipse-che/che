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
package org.eclipse.che.ide.command.toolbar.previews;

import static com.google.gwt.dom.client.Style.Unit.PX;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropdownListItemRenderer;

/**
 * Renders widgets for the 'Previews' list. Always returns the same instance of the header widget
 * which is shared among all {@link PreviewUrl}s.
 */
class PreviewUrlItemRenderer implements DropdownListItemRenderer {

  static final HeaderWidget HEADER_WIDGET = new HeaderWidget();

  private final BaseListItem<PreviewUrl> item;

  private Widget listWidget;

  PreviewUrlItemRenderer(BaseListItem<PreviewUrl> item) {
    this.item = item;
  }

  @Override
  public Widget renderHeaderWidget() {
    return HEADER_WIDGET;
  }

  @Override
  public Widget renderListWidget() {
    if (listWidget == null) {
      listWidget = new Label(item.getValue().getDisplayName());
      listWidget.getElement().getStyle().setMarginBottom(0, PX);
    }

    return listWidget;
  }

  private static class HeaderWidget extends Label {

    private static final CommandResources RESOURCES = GWT.create(CommandResources.class);

    HeaderWidget() {
      super();

      addStyleName(RESOURCES.commandToolbarCss().previewUrlWidget());

      final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
      safeHtmlBuilder.appendHtmlConstant(FontAwesome.BULLSEYE);
      getElement().setInnerSafeHtml(safeHtmlBuilder.toSafeHtml());
    }
  }
}
