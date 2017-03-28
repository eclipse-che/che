/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.toolbar.previews;

import elemental.dom.Element;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropdownListItemRenderer;

import static com.google.gwt.dom.client.Style.Unit.PX;
import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

/** Renders widgets for the 'Previews' list. Always returns the same instance of header widget. */
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
        private static final ToolbarMessages  MESSAGES  = GWT.create(ToolbarMessages.class);

        HeaderWidget() {
            super();

            addStyleName(RESOURCES.commandToolbarCss().previewUrlWidget());

            final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
            safeHtmlBuilder.appendHtmlConstant(FontAwesome.BULLSEYE);
            getElement().setInnerSafeHtml(safeHtmlBuilder.toSafeHtml());

            Tooltip.create((Element)getElement(), BOTTOM, MIDDLE, MESSAGES.previewsTooltip());
        }
    }
}
