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
package org.eclipse.che.ide.command.toolbar.previewurl;

import elemental.dom.Element;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.StringItemRenderer;

import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

/** Renders widgets for the 'Previews' list. Always returns the same instance of header widget. */
class PreviewUrlItemRenderer extends StringItemRenderer {

    static final HeaderWidget HEADER_WIDGET = new HeaderWidget();

    PreviewUrlItemRenderer(BaseListItem<String> item) {
        super(item);
    }

    @Override
    public Widget renderHeaderWidget() {
        return HEADER_WIDGET;
    }

    private static class HeaderWidget extends Label {

        private static final CommandResources RESOURCES = GWT.create(CommandResources.class);

        HeaderWidget() {
            super();

            addStyleName(RESOURCES.commandToolbarCss().previewUrlWidget());

            final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
            safeHtmlBuilder.appendHtmlConstant(FontAwesome.BULLSEYE);
            getElement().setInnerSafeHtml(safeHtmlBuilder.toSafeHtml());

            Tooltip.create((Element)getElement(), BOTTOM, MIDDLE, "Previews");
        }
    }
}
