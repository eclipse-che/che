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



import com.google.inject.Inject;

import org.eclipse.che.datasource.ide.newDatasource.InitializableWizardDialog;
import org.eclipse.che.datasource.ide.newDatasource.NewDatasourceWizard;
import org.eclipse.che.datasource.ide.newDatasource.NewDatasourceWizardFactory;
import org.eclipse.che.datasource.ide.newDatasource.view.NewDatasourceWizardHeadView;
import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.dto.DtoFactory;


public class NewDatasourceWizardPresenter implements Wizard.UpdateDelegate,
                                                     NewDatasourceWizardHeadView.ActionDelegate, InitializableWizardDialog<DatabaseConfigurationDTO> {

    private final     NewDatasourceWizardFactory  wizardFactory;
    private           NewDatasourceWizardHeadView view;
    private final DtoFactory                  dtoFactory;
    /** The new datasource wizard template. */
    private       NewDatasourceWizard         wizard;
    private DatabaseConfigurationDTO configuration;
    private       NewDatasourceWizardMainPagePresenter categoriesListPage;

    @Inject
    public NewDatasourceWizardPresenter(NewDatasourceWizardHeadView view,
                                        NewDatasourceWizardMainPagePresenter categoriesListPage,
                                        DtoFactory dtoFactory,
                                        NewDatasourceWizardFactory wizardFactory
                                        ) {

        this.categoriesListPage = categoriesListPage;
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.wizardFactory = wizardFactory;
        this.view.setDelegate(this);
    }


    public void show() {
        wizard = wizardFactory.create(configuration != null ? configuration : dtoFactory.createDto(DatabaseConfigurationDTO.class));
        wizard.setUpdateDelegate(this);
        wizard.addPage(categoriesListPage);
        showFirstPage();
    }

    @Override
    public void initData(DatabaseConfigurationDTO configuration) {
        this.configuration = configuration;
    }


    @Override
    public void datasourceNameChanged(String name) {}

    private void showFirstPage() {
    }

    @Override
    public void onSaveClicked(){}

    @Override
    public void onCancelClicked(){
        view.cleanPage("settings");
        view.close();
    }


    @Override
    public void updateControls() {
//        view.cleanPage("settings");

//        updateButtonsState();
    }


}
