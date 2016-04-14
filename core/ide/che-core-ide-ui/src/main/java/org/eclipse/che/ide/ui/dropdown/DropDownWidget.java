/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ui.dropdown;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.Action;

/**
 * Provides methods which allow change visual representation of header of the list.
 *
 * @author Valeriy Svydenko
 * @author Oleksii Orel
 */
public interface DropDownWidget {
    /** returns name of the selected element* */
    @Nullable
    String getSelectedName();

    /** returns id of the selected element* */
    @Nullable
    String getSelectedId();

    /**
     * Sets id and name of selected element.
     *
     * @param id
     *         id of the selected element
     * @param name
     *         name of the selected element
     */
    void selectElement(@Nullable String id, @Nullable String name);

    /**
     * Create an instance of element action with given name amd id for displaying it.
     *
     * @param id
     *         id of element
     * @param name
     *         name of element
     *
     * @return an instance of selector action
     */
    Action createAction(String id, String name);

    /**
     * Update popup elements in drop down part of widget.
     */
    void updatePopup();

}