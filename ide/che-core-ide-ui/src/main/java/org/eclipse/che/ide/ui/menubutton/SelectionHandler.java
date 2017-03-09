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
package org.eclipse.che.ide.ui.menubutton;

/** Selection handler for {@link MenuPopupButton}. */
public interface SelectionHandler {

    /**
     * Called when item has been selected in {@link MenuPopupButton}.
     *
     * @param item
     *         the selected item
     */
    void onItemSelected(PopupItem item);
}
