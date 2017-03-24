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

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.util.Pair;

import java.util.List;

/** Data provider for {@link MenuPopupButton}. */
public interface MenuPopupItemDataProvider {

    /** Returns the default item, used when user click on button. */
    @Nullable
    PopupItem getDefaultItem();

    /** Returns top level items. */
    List<PopupItem> getItems();

    /**
     * Checks whether the given {@code item} is a group.
     * Group item cannot be selected.
     */
    boolean isGroup(PopupItem item);

    /** Returns the pair of the given {@code parent} children and their labels. */
    @Nullable
    Pair<List<PopupItem>, String> getChildren(PopupItem parent);

    /** Sets the {@link ItemDataChangeHandler}. */
    void setItemDataChangedHandler(ItemDataChangeHandler handler);

    interface ItemDataChangeHandler {
        /** Should be called when {@link MenuPopupItemDataProvider}'s data has been changed. */
        void onItemDataChanged();
    }
}
