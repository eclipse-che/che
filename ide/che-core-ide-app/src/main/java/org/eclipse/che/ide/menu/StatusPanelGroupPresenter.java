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
package org.eclipse.che.ide.menu;

import org.eclipse.che.ide.api.mvp.Presenter;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manages status panel group items, their runtime visibility and enabled state.
 *
 * @author Oleksii Orel
 */
@Singleton
public class StatusPanelGroupPresenter implements Presenter, StatusPanelGroupView.ActionDelegate {

    private final StatusPanelGroupView view;

    /**
     * Bottom Menu Presenter requires View implementation
     *
     * @param view
     */
    @Inject
    public StatusPanelGroupPresenter(StatusPanelGroupView view) {
        this.view = view;
        this.view.setDelegate(this);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
