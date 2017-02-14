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
 * Renderer of the items for {@link DropDownList}.
 * <p><b>Important:</b> {@link #renderHeaderWidget()} and {@link #renderListWidget()} mustn't return the same instance.
 *
 * @see StringItemRenderer
 */
public interface DropDownListItemRenderer {

    /** Returns widget for representing the {@link DropDownListItem} in the list's header (chosen item). */
    Widget renderHeaderWidget();

    /** Returns widget for representing the {@link DropDownListItem} in the list. */
    Widget renderListWidget();
}
