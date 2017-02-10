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
package org.eclipse.che.ide.ui.dropdown;

import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract implementation of {@link DropDownListItemRenderer}.
 * <p>By default, {@link #renderHeaderWidget()} and {@link #renderListWidget()} delegate rendering to {@link #getWidget()}.
 */
public abstract class AbstractListItemRenderer<T extends DropDownListItem> implements DropDownListItemRenderer {

    protected final T item;

    public AbstractListItemRenderer(T item) {
        this.item = item;
    }

    @Override
    public Widget renderListWidget() {
        return getWidget();
    }

    @Override
    public Widget renderHeaderWidget() {
        return getWidget();
    }

    protected abstract Widget getWidget();
}
