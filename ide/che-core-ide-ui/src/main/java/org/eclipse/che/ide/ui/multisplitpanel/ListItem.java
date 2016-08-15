/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 ******************************************************************************/
package org.eclipse.che.ide.ui.multisplitpanel;

import org.eclipse.che.ide.api.mvp.View;

/**
 * @author Dmitry Shnurenko
 * @author Vitaliy Guliy
 */
public interface ListItem<T> extends View<ListItem.ActionDelegate> {

    /**
     * Returns associated tab item.
     *
     * @return tab item
     */
    T getTabItem();

    interface ActionDelegate {

        /**
         * Handle clicking on list item
         *
         * @param listItem
         */
        void onItemClicked(ListItem listItem);

        /**
         * Handle clicking on close icon
         *
         * @param listItem
         */
        void onCloseButtonClicked(ListItem listItem);
    }
}
