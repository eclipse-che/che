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
package org.eclipse.che.datasource.ide.newDatasource.connector;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.datasource.ide.DatasourceClientService;
import org.eclipse.che.datasource.ide.newDatasource.NewDatasourceWizardMessages;
import org.eclipse.che.datasource.shared.ConnectionTestResultDTO;
import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;
import org.eclipse.che.datasource.shared.DatabaseType;
import org.eclipse.che.datasource.shared.TextDTO;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

public abstract class AbstractNewDatasourceConnectorPage extends AbstractWizardPage<DatabaseConfigurationDTO>
        implements AbstractNewDatasourceConnectorView.ActionDelegate {

    private final AbstractNewDatasourceConnectorView view;
    private final DatasourceClientService            service;
    private final NotificationManager                notificationManager;
    private final DtoFactory                         dtoFactory;
    private final NewDatasourceWizardMessages        messages;

    public AbstractNewDatasourceConnectorPage(@NotNull final AbstractNewDatasourceConnectorView view,
                                              @NotNull final DatasourceClientService service,
                                              @NotNull final NotificationManager notificationManager,
                                              @NotNull final DtoFactory dtoFactory,
                                              @NotNull final NewDatasourceWizardMessages messages) {
        super();
        view.setDelegate(this);
        this.service = service;
        this.view = view;
        this.notificationManager = notificationManager;
        this.dtoFactory = dtoFactory;
        this.messages = messages;

    }


    @Override
    public void init(DatabaseConfigurationDTO dataObject) {
        super.init(dataObject);

        dataObject.setDatabaseType(getDatabaseType());
        dataObject.setPort(getDefaultPort());
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        Log.info(AbstractNewDatasourceConnectorPage.class,"Executing go");
        container.setWidget(getView().asWidget());
    }

    public AbstractNewDatasourceConnectorView getView() {
        return view;
    }

    /**
     * Returns the currently configured database.
     *
     * @return the database
     */
    protected abstract DatabaseConfigurationDTO getConfiguredDatabase();

    @Override
    public void onClickTestConnectionButton() {
        if (getView().isPasswordFieldDirty()) {
            getView().setEncryptedPassword(getView().getPassword(), false);
            Log.info(AbstractNewDatasourceConnectorPage.class,"clicked");
            doOnClickTestConnectionButton();

        }
        else {
            doOnClickTestConnectionButton();
        }

    }

    public void doOnClickTestConnectionButton() {
        DatabaseConfigurationDTO configuration = getConfiguredDatabase();
        Log.info(AbstractNewDatasourceConnectorPage.class,"config done");

        final Notification connectingNotification = new Notification(messages.startConnectionTest());
//        notificationManager.showNotification(connectingNotification);

        try {
            Log.info(AbstractNewDatasourceConnectorPage.class,"try service");

            service.testDatabaseConnectivity(configuration, new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                @Override
                protected void onSuccess(String result) {
                    Log.info(AbstractNewDatasourceConnectorPage.class,"Success");

                    final ConnectionTestResultDTO testResult = dtoFactory.createDtoFromJson(result, ConnectionTestResultDTO.class);
                    if (ConnectionTestResultDTO.Status.SUCCESS.equals(testResult.getTestResult())) {

                        getView().onTestConnectionSuccess();
//                        connectingNotification.setState(messages.connectionTestSuccessNotification());
//                        connectingNotification.setStatus(Notification.Status.FINISHED);
                    } else {
                        getView().onTestConnectionFailure(messages.connectionTestFailureSuccessMessage() + " "
                                                          + testResult.getFailureMessage());
//                        connectingNotification.setMessage(messages.connectionTestFailureSuccessNotification());
//                        connectingNotification.setType(Type.ERROR);
//                        connectingNotification.setStatus(Notification.S`tatus.FINISHED);
                    }
                }

                @Override
                protected void onFailure(final Throwable exception) {
                    Log.info(AbstractNewDatasourceConnectorPage.class,"Failure");

                    getView().onTestConnectionFailure(messages.connectionTestFailureSuccessMessage());
//                    connectingNotification.setMessage(messages.connectionTestFailureSuccessNotification());
//                    connectingNotification.setType(Type.ERROR);
//                    connectingNotification.setStatus(Notification.Status.FINISHED);
                }
            }

                   );
        } catch (final RequestException e) {
            Log.debug(AbstractNewDatasourceConnectorPage.class, e.getMessage());
            getView().onTestConnectionFailure(messages.connectionTestFailureSuccessMessage());
//            connectingNotification.setMessage(messages.connectionTestFailureSuccessNotification());
//            connectingNotification.setType(Type.ERROR);
//            connectingNotification.setStatus(Notification.Status.FINISHED);
        }
    }

    @Override
    public void databaseNameChanged(String name) {
        dataObject.setDatabaseName(name);
        updateDelegate.updateControls();
    }

    @Override
    public void userNameChanged(String name) {
        dataObject.setUsername(name);
        updateDelegate.updateControls();
    }

    @Override
    public void passwordChanged(String password) {
        dataObject.setPassword(password);
        updateDelegate.updateControls();
    }

    public abstract Integer getDefaultPort();

    public abstract DatabaseType getDatabaseType();
}
