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

import com.google.inject.assistedinject.Assisted;

import javax.validation.constraints.NotNull;

/**
 * The factory for creating drop down list.
 *
 * @author Valeriy Svydenko
 */
public interface DropDownListFactory {
    /**
     * Create an instance of {@link DropDownHeaderWidget} with a given identifier for registering.
     *
     * @param listId
     *         list identifier
     * @return an instance of {@link DropDownHeaderWidget}
     */
    @NotNull
    DropDownHeaderWidget createList(@NotNull String listId);

    /**
     * Create an instance of {@link SimpleListElementAction} with given name amd icon for displaying it and header which is configured this
     * element.
     *
     * @param id
     *         id of action
     * @param name
     *         name of action
     * @param header
     *         header widget of custom list
     * @return an instance of {@link SimpleListElementAction}
     */
    @NotNull
    SimpleListElementAction createElement(@NotNull @Assisted("id") String id, @NotNull @Assisted("name") String name, DropDownHeaderWidget header);
}
