/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.tutorial.parts.part;

import org.eclipse.che.ide.api.parts.base.BasePresenter;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.Nonnull;

/**
 * The presenter that represent general part for showing part API.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public class MyPartPresenter extends BasePresenter implements MyPartView.ActionDelegate {
    private MyPartView view;
    private String     title;

    @Inject
    public MyPartPresenter(MyPartView view, @Assisted String title) {
        this.view = view;
        this.view.setDelegate(this);
        this.view.setTitle(title);
        this.title = title;
    }

    /** {@inheritDoc} */
    @Override
    public void onButtonClicked() {
        Window.alert(title);
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SVGResource getTitleSVGImage() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitleToolTip() {
        return "Tooltip";
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}