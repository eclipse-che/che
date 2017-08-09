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
package org.eclipse.che.datasource.ide.newDatasource.presenter;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;


import org.eclipse.che.datasource.ide.newDatasource.view.NewDatasourceWizardMainPageView;
import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;


public class NewDatasourceWizardMainPagePresenter extends AbstractWizardPage<DatabaseConfigurationDTO>
        implements NewDatasourceWizardMainPageView.ActionDelegate {

    protected NewDatasourceWizardMainPageView    view;

    @Inject
    public NewDatasourceWizardMainPagePresenter(NewDatasourceWizardMainPageView view) {
        super();
        this.view = view;
        this.view.setDelegate(this);
    }

    public void go(AcceptsOneWidget container) {
        container.setWidget(view.asWidget());


    }


}
