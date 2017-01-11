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

import javax.validation.constraints.NotNull;

/**
 * The factory for creating drop down list.
 *
 * @author Valeriy Svydenko
 */
public interface DropDownListFactory {

    /**
     * Create an instance of {@link DropDownWidget} managing action group registered in action manager.
     *
     * @param actionGroupId
     *          identifier of {@link org.eclipse.che.ide.api.action.ActionGroup} registered in action manager
     *
     * @return
     *          an instance of {@link DropDownWidget}
     */
    @NotNull
    DropDownWidget createDropDown(@NotNull String actionGroupId);

}
