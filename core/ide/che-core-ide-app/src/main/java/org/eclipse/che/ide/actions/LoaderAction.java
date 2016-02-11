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
package org.eclipse.che.ide.actions;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter.State;
import static org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter.State.WORKING;

/**
 * Action for displaying information about a process of loading.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class LoaderAction extends Action implements CustomComponentAction, LoaderPresenter.LoaderStateListener {

    private final LoaderPresenter loader;
    private State loaderState;

    @Inject
    public LoaderAction(LoaderPresenter loader) {
        super("loader", "loader action");
        this.loaderState = WORKING;
        this.loader = loader;
        this.loader.setListener(this);
    }

    @Override
    public final void update(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(WORKING.equals(loaderState));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        return loader.getCustomComponent();
    }

    @Override
    public void onLoaderStateChanged(LoaderPresenter.State state) {
        loaderState = state;
    }
}
