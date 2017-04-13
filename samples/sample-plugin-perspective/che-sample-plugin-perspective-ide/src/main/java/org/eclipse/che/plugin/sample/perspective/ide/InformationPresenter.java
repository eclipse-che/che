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
package org.eclipse.che.plugin.sample.perspective.ide;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 */
@Singleton
public class InformationPresenter extends BasePresenter {

    private LocalizationConstant localizationConstant;
    private InformationView view;

    @Inject
    public InformationPresenter(LocalizationConstant localizationConstant, InformationView view){
        this.localizationConstant = localizationConstant;
        this.view = view;
    }

    @Override
    public String getTitle() {
        return localizationConstant.informationTitle();
    }

    @Override
    public SVGResource getTitleImage() {
        return (CustomPerspectiveResources.INSTANCE.icon());
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public String getTitleToolTip() {
        return localizationConstant.informationTitle();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
