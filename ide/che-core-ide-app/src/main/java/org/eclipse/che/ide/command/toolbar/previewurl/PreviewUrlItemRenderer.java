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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.StringItemRenderer;

/** {@link StringItemRenderer} which always returns the same header widget. */
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
        HeaderWidget() {
            super();

            final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
            safeHtmlBuilder.appendHtmlConstant(FontAwesome.BULLSEYE);
            getElement().setInnerSafeHtml(safeHtmlBuilder.toSafeHtml());
            getElement().getStyle().setColor("#4eabff");
        }
    }
}
