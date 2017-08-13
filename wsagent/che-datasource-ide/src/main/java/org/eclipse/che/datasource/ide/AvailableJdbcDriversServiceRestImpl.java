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
package org.eclipse.che.datasource.ide;

import com.google.gwt.http.client.RequestException;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.datasource.ide.events.JdbcDriversFetchedEvent;
import org.eclipse.che.datasource.ide.newDatasource.presenter.NewDatasourceWizardMainPagePresenter;
import org.eclipse.che.datasource.shared.DriversDTO;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.util.loging.Log;

import java.util.List;

public class AvailableJdbcDriversServiceRestImpl implements AvailableJdbcDriversService {

    List<String>                      drivers;
    protected DatasourceClientService datasourceClientService;
    protected DtoFactory              dtoFactory;
    protected NotificationManager     notificationManager;
    protected EventBus                eventBus;

    @Inject
    public AvailableJdbcDriversServiceRestImpl(DatasourceClientService datasourceClientService,
                                               DtoFactory dtoFactory,
                                               NotificationManager notificationManager, EventBus eventBus) {
        this.datasourceClientService = datasourceClientService;
        this.dtoFactory = dtoFactory;
        this.notificationManager = notificationManager;
        this.eventBus = eventBus;
    }

    @Override
    public void fetch() {
        try {
            datasourceClientService.getAvailableDrivers(new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                @Override
                protected void onSuccess(String result) {
                    DriversDTO driversDto = dtoFactory.createDtoFromJson(result, DriversDTO.class);
                    drivers = driversDto.getDrivers();
                    eventBus.fireEvent(new JdbcDriversFetchedEvent(drivers));
                }

                @Override
                protected void onFailure(Throwable exception) {
                    notificationManager.notify(new Notification("Failed getting available JDBC drivers from server: "));
                }
            });
        } catch (RequestException e) {
            notificationManager.notify(new Notification("Failed getting available JDBC drivers from server: " + e.getMessage()));
        }
    }

    @Override
    public List<String> getDrivers() {
        return drivers;
    }

}
