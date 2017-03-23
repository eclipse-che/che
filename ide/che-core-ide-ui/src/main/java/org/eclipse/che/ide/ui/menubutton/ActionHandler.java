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

/** Action handler for {@link MenuPopupButton}. */
public interface ActionHandler {

    /**
     * Called when action (mouse click) on the {@code item}has been performed.
     *
     * @param item
     *         the item on which action has been performed
     */
    void onAction(PopupItem item);
}
