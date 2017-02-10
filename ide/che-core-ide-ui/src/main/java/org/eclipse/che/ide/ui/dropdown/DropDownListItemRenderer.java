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
 * @param <T>
 *         type of the item for which need to render widgets
 */
public interface DropDownListItemRenderer<T extends DropDownListItem> {

    /** Produces a new instance of the widget for representing the given {@code item}. */
    Widget render(T item);
}
