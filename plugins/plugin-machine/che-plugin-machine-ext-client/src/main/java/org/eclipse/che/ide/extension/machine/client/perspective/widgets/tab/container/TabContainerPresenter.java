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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.Tab;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The class provides methods which contains business logic to add and control tabs.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class TabContainerPresenter implements TabHeader.ActionDelegate {

    private final List<Tab>        tabs;
    private final TabContainerView view;

    @Inject
    public TabContainerPresenter(TabContainerView view) {
        this.view = view;

        this.tabs = new ArrayList<>();
    }

    /** @return view which associated with current tab. */
    public TabContainerView getView() {
        return view;
    }

    /**
     * Adds tab to container. Tab contains header and associated content.
     *
     * @param tab
     *         tab which need add
     */
    public void addTab(@NotNull Tab tab) {
        TabHeader header = tab.getHeader();
        header.setDelegate(this);

        TabPresenter content = tab.getContent();

        tabs.add(tab);

        view.addHeader(header);
        view.addContent(content);
    }

    /**
     * Shows content of clicked tab.
     *
     * @param tabName
     *         name of tab which need show
     */
    public void showTab(@NotNull String tabName) {
        onTabClicked(tabName);
    }

    /** {@inheritDoc} */
    @Override
    public void onTabClicked(@NotNull String tabName) {
        for (Tab tab : tabs) {
            TabHeader header = tab.getHeader();
            TabPresenter content = tab.getContent();

            if (tabName.equals(header.getName())) {
                header.setEnable();

                content.setVisible(true);

                tab.performHandler();

                continue;
            }

            header.setDisable();
            content.setVisible(false);
        }
    }
}
