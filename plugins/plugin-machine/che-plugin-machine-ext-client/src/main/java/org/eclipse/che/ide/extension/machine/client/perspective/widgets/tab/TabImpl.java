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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container.TabContainerView.TabSelectHandler;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * The class describes tab entity which contains header and associated content, and provides methods to get tab's header or content.
 *
 * @author Dmitry Shnurenko
 */
public class TabImpl implements Tab {

    private final TabHeader        tabHeader;
    private final TabPresenter     tabPresenter;
    private final TabSelectHandler handler;

    @Inject
    public TabImpl(@Assisted TabHeader tabHeader, @Assisted TabPresenter tabPresenter, @Nullable @Assisted TabSelectHandler handler) {
        this.tabHeader = tabHeader;
        this.tabPresenter = tabPresenter;
        this.handler = handler;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public TabHeader getHeader() {
        return tabHeader;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public TabPresenter getContent() {
        return tabPresenter;
    }

    /** {@inheritDoc} */
    @Override
    public void performHandler() {
        if (handler == null) {
            return;
        }

        handler.onTabSelected();
    }
}
