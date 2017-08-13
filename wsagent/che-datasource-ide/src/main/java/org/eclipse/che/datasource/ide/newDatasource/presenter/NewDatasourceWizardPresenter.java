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

import org.eclipse.che.datasource.ide.DatasourceClientService;
import org.eclipse.che.datasource.ide.InitializableWizardPage;
import org.eclipse.che.datasource.ide.newDatasource.InitializableWizardDialog;
import org.eclipse.che.datasource.ide.newDatasource.NewDatasourceWizard;
import org.eclipse.che.datasource.ide.newDatasource.NewDatasourceWizardFactory;
import org.eclipse.che.datasource.ide.newDatasource.connector.AbstractNewDatasourceConnectorPage;
import org.eclipse.che.datasource.ide.newDatasource.connector.NewDatasourceConnector;
import org.eclipse.che.datasource.ide.newDatasource.connector.NewDatasourceConnectorAgent;
import org.eclipse.che.datasource.ide.newDatasource.presenter.NewDatasourceWizardMainPagePresenter;
import org.eclipse.che.datasource.ide.newDatasource.view.NewDatasourceWizardHeadView;
import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;
import org.eclipse.che.datasource.shared.TextDTO;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.api.wizard.WizardPage;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.annotation.Nullable;

public class NewDatasourceWizardPresenter implements InitializableWizardDialog<DatabaseConfigurationDTO>,
                                                     Wizard.UpdateDelegate, NewDatasourceWizardHeadView.ActionDelegate,
                                                     NewDatasourceWizardMainPagePresenter.ConnectorSelectedListener {

    private final NewDatasourceWizardFactory           wizardFactory;
    private final DtoFactory                           dtoFactory;
    private final DialogFactory                        dialogFactory;
    private final DatasourceClientService              service;
    private final NewDatasourceConnectorAgent          newDatasourceConnectorAgent;
    private       NewDatasourceWizardHeadView          view;
    private       NewDatasourceWizardMainPagePresenter categoriesListPage;
    private       AbstractNewDatasourceConnectorPage   connectorPage;

    /** The new datasource wizard template. */
    private NewDatasourceWizard wizard;

    private DatabaseConfigurationDTO configuration;

    @Inject
    public NewDatasourceWizardPresenter(NewDatasourceWizardHeadView view,
                                        NewDatasourceWizardMainPagePresenter categoriesListPage,
                                        NewDatasourceWizardFactory wizardFactory,
                                        DtoFactory dtoFactory,
                                        DialogFactory dialogFactory,
                                        DatasourceClientService service,
                                        NewDatasourceConnectorAgent newDatasourceConnectorAgent) {
        this.view = view;
        this.categoriesListPage = categoriesListPage;
        this.categoriesListPage.setConnectorSelectedListener(this);
        this.wizardFactory = wizardFactory;
        this.dtoFactory = dtoFactory;
        this.dialogFactory = dialogFactory;
        this.service = service;
        this.newDatasourceConnectorAgent = newDatasourceConnectorAgent;
        this.view.setDelegate(this);
    }

    @Override
    public void onSaveClicked() {
//        if (connectorPage.getView().isPasswordFieldDirty()) {
//            try {
//                service.encryptText(connectorPage.getView().getPassword(), new AsyncRequestCallback<String>(new StringUnmarshaller()) {
//                    @Override
//                    protected void onSuccess(final String encryptedText) {
//                        TextDTO encryptedTextDTO = dtoFactory.createDtoFromJson(encryptedText, TextDTO.class);
//                        connectorPage.getView().setEncryptedPassword(encryptedTextDTO.getValue(), false);
//
//                        completeWizard();
//                    }
//
//                    @Override
//                    protected void onFailure(Throwable exception) {
//                        Log.error(DefaultNewDatasourceConnectorViewImpl.class, exception);
//                    }
//                });
//            } catch (RequestException e2) {
//                Log.error(DefaultNewDatasourceConnectorViewImpl.class, e2);
//            }
//        } else {
//            completeWizard();
//        }
    }

    private void completeWizard() {
        wizard.complete(new Wizard.CompleteCallback() {
            @Override
            public void onCompleted() {
                view.cleanPage("settings");
                view.close();
            }

            @Override
            public void onFailure(Throwable e) {
                dialogFactory.createMessageDialog("", e.getMessage(), null);
            }
        });
    }

    @Override
    public void onCancelClicked() {
        view.cleanPage("settings");
        view.close();
    }

    @Override
    public void updateControls() {
        view.cleanPage("settings");
        updateButtonsState();
    }

    private void updateButtonsState() {
        String name = wizard.getContext().get(NewDatasourceWizard.DATASOURCE_NAME_KEY);
        view.setFinishButtonEnabled((categoriesListPage != null && categoriesListPage.isCompleted()
                                     && connectorPage != null && connectorPage.isCompleted())
                                    && (name != null) && (name.length() > 0));
    }

    public void show() {
        wizard = wizardFactory.create(configuration != null ? configuration : dtoFactory.createDto(DatabaseConfigurationDTO.class));
        wizard.setUpdateDelegate(this);
        wizard.addPage(categoriesListPage);

        showFirstPage();
    }

    @Override
    public void datasourceNameChanged(String name) {
        wizard.getDataObject().setDatasourceId(name);
        wizard.getContext().put(NewDatasourceWizard.DATASOURCE_NAME_KEY, name);

        RegExp regExp = RegExp.compile("^\\S.*$");
        if (regExp.test(name)) {
            view.removeNameError();
        } else {
            view.showNameError();
        }
        updateButtonsState();
    }

    private void showFirstPage() {
        view.reset();

        view.showPage(wizard.navigateToFirst(), "categories");
        view.showDialog();
        view.setEnabledAnimation(true);

        if (configuration != null) {
            String datasourceID = configuration.getDatasourceId();
            view.setName(datasourceID);
            datasourceNameChanged(datasourceID);
        } else {
            updateButtonsState();
        }
    }

    @Override
    public void onConnectorSelected(@Nullable String id) {
        if (id == null) {
            // category selected
            view.cleanPage("settings");
            return;
        }

        NewDatasourceConnector connector = newDatasourceConnectorAgent.getConnector(id);
        if (connector != null) {
            for (Provider<? extends AbstractNewDatasourceConnectorPage> provider : connector.getWizardPages()) {
                connectorPage = provider.get();
                connectorPage.setContext(wizard.getContext());
                connectorPage.setUpdateDelegate(this);
                connectorPage.init(wizard.getDataObject());
                if (configuration != null) {
                    initPage(connectorPage);
                }
                break;
            }
            view.showPage(connectorPage, "settings");
        }
    }

    @Override
    public void initData(DatabaseConfigurationDTO configuration) {
        this.configuration = configuration;
    }

    private void initPage(final WizardPage page) {
        Log.info(NewDatasourceWizardPresenter.class, "Initializing wizard page : " + page.getClass());
        ((InitializableWizardPage)page).initPage(configuration);
        Log.info(NewDatasourceWizardPresenter.class, "Wizard page initialization done");
    }
}
