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

/**
 * Data provider for {@link MenuPopupButton}
 */
public interface PopupItemDataProvider {

    /**
     * The default or last selected item, used when user click on button
     *
     * @return
     */
    @Nullable
    PopupItem getDefaultItem();

    /**
     * Top level items
     *
     * @return
     */
    List<PopupItem> getItems();

    /**
     * Checks if item is a group, group item cannot be selected
     *
     * @param popupItem
     * @return
     */
    boolean isGroup(PopupItem popupItem);

    /**
     * @param parent
     * @return the pair of list children and their label
     */
    @Nullable
    Pair<List<PopupItem>, String> getChildren(PopupItem parent);

    /**
     * Used for notification about changing  data provider
     *
     * @param handler
     */
    void setItemDataChangedHandler(ItemDataChangeHandler handler);


    interface ItemDataChangeHandler {
        void onItemDataChanged();
    }
}
