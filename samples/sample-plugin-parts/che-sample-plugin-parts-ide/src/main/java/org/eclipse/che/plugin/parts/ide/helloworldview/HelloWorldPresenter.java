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

package org.eclipse.che.plugin.parts.ide.helloworldview;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.plugin.parts.ide.SamplePartsResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Presenter for the sample Hello World View.
 *
 * @author Edgar Mueller
 */
@Singleton
public class HelloWorldPresenter extends BasePresenter {

    private HelloWorldView view;

    @Inject
    public HelloWorldPresenter(HelloWorldView view){
        this.view = view;
    }

    @Override
    public String getTitle() {
        return "Hello World View";
    }

    @Override
    public SVGResource getTitleImage() {
        return (SamplePartsResources.INSTANCE.icon());
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public String getTitleToolTip() {
        return "Hello World Tooltip";
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
