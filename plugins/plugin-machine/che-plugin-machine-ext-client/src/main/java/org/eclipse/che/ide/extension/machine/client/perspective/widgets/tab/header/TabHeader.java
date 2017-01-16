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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import javax.validation.constraints.NotNull;

/**
 * The interface provides methods which allow control displaying of tab's header.There is ability to use default tab header, or
 * create custom. If you want create custom header you have to implement this interface.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(TabHeaderImpl.class)
public interface TabHeader extends IsWidget {
    /** Sets tab enable. */
    void setEnable();

    /** Sets tab disable. */
    void setDisable();

    /** @return name of tab. */
    @NotNull
    String getName();

    /**
     * Sets special delegate which will contain business logic to response on user's actions.
     *
     * @param delegate
     *         delegate which need set
     */
    void setDelegate(@NotNull ActionDelegate delegate);

    interface ActionDelegate {
        /**
         * Performs some actions when user click on tab.
         *
         * @param tabName
         *         name of tab on which was clicked
         */
        void onTabClicked(@NotNull String tabName);
    }
}
