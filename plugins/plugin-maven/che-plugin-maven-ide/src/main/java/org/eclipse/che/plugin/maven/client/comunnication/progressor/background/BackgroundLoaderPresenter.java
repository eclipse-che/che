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
package org.eclipse.che.plugin.maven.client.comunnication.progressor.background;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.maven.client.comunnication.progressor.ResolveDependencyPresenter;

/**
 * Loader for displaying information about resolving dependencies.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class BackgroundLoaderPresenter implements BackgroundLoaderView.ActionDelegate {
    private final ResolveDependencyPresenter resolveDependencyPresenter;
    private final BackgroundLoaderView       view;

    @Inject
    public BackgroundLoaderPresenter(ResolveDependencyPresenter resolveDependencyPresenter,
                                     BackgroundLoaderView view) {
        this.resolveDependencyPresenter = resolveDependencyPresenter;
        this.view = view;
        this.view.setDelegate(this);
    }

    /**
     * @return custom Widget that represents the loader's action in UI.
     */
    public Widget getCustomComponent() {
        return view.asWidget();
    }

    /** Hide the loader. */
    public void hide() {
        view.hide();
        resolveDependencyPresenter.hide();
    }

    /** Show the loader. */
    public void show() {
        view.show();
    }

    /**
     * Set label into loader which describes current state of loader.
     *
     * @param text
     *         message of the status
     */
    public void setProgressLabel(String text) {
        view.setOperationLabel(text);
        resolveDependencyPresenter.setProgressLabel(text);
    }

    /**
     * Change the value of resolved modules of the project.
     *
     * @param percentage
     *         value of resolved modules
     */
    public void updateProgressBar(int percentage) {
        view.updateProgressBar(percentage);
        resolveDependencyPresenter.updateProgressBar(percentage);
    }

    @Override
    public void showResolverInfo() {
        resolveDependencyPresenter.show();
    }

}
