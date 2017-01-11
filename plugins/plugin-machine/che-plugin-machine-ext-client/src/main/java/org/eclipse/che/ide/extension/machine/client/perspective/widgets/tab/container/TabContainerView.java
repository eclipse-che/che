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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;

import javax.validation.constraints.NotNull;

/**
 * Provides methods which allows add header of tab and tab's content to special container.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(TabContainerViewImpl.class)
public interface TabContainerView extends IsWidget {
    /**
     * Adds tab header to container.
     *
     * @param tabHeader
     *         header which need add
     */
    void addHeader(@NotNull TabHeader tabHeader);

    /**
     * Adds tab content to container.
     *
     * @param content
     *         content which need add
     */
    void addContent(@NotNull TabPresenter content);

    interface TabSelectHandler {
        /** Performs some actions when user clicks on tab. */
        void onTabSelected();
    }
}
