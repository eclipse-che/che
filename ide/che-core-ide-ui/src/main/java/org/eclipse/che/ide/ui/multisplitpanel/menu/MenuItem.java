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
package org.eclipse.che.ide.ui.multisplitpanel.menu;

import org.eclipse.che.ide.api.mvp.View;

/**
 * Item of the {@link Menu}.
 *
 * @author Artem Zatsarynnyi
 */
public interface MenuItem<T> extends View<MenuItem.ActionDelegate> {

    /** Returns associated data. */
    T getData();

    interface ActionDelegate {

        /** Called when {@code menuItem} has been selected. */
        void onItemSelected(MenuItem menuItem);

        /** Called when {@code menuItem} is going to be closed. */
        void onItemClosing(MenuItem menuItem);
    }
}
