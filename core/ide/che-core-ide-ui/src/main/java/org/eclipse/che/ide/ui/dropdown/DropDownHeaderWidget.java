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

import javax.validation.constraints.NotNull;

/**
 * Provides methods which allow change visual representation of header of the list.
 *
 * @author Valeriy Svydenko
 */
public interface DropDownHeaderWidget {
    /** Sets the action delegate. */
    void setDelegate(ActionDelegate delegate);

    /** returns title of the selected element* */
    @Nullable
    String getSelectedElementName();

    /**
     * Sets title and image of selected element.
     *
     * @param title
     *         title of the selected element
     */
    void selectElement(@NotNull String title);

    interface ActionDelegate {
        void onSelect();
    }

}