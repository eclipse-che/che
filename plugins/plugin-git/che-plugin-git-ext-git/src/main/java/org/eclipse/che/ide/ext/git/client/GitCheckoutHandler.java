/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client;


import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.resources.impl.ResourceManager;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Receives git checkout notifications caught by server side VFS file watching system.
 * Support two type of notifications: git branch checkout and git revision checkout.
 * After a notification is received it is processed and passed to an instance of
 * {@link NotificationManager}. Updates attributes of the project from server to load
 * new HEAD value to project attributes that are held in {@link ResourceManager}.
 */
@Singleton
public class GitCheckoutHandler {
    private final Provider<NotificationManager>          notificationManagerProvider;
    private final AppContext                             appContext;

    @Inject
    public GitCheckoutHandler(Provider<NotificationManager> notificationManagerProvider,
                              RequestHandlerConfigurator configurator,
                              AppContext appContext) {
        this.notificationManagerProvider = notificationManagerProvider;
        this.appContext = appContext;

        configureHandler(configurator);
    }

    private void configureHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("event:git-checkout")
                    .paramsAsDto(GitCheckoutEventDto.class)
                    .noResult()
                    .withBiConsumer(this::apply);
    }

    public void apply(String endpointId, GitCheckoutEventDto dto) {
        final Type type = dto.getType();
        final String name = dto.getName();

        switch (type) {
            case BRANCH: {
                Log.debug(getClass(), "Received git branch checkout event: " + name);

                final NotificationManager notificationManager = notificationManagerProvider.get();
                if (notificationManager != null) {
                    notificationManager.notify("Git operation", "Branch '" + name + "' is checked out", SUCCESS, EMERGE_MODE);
                }

                break;
            }
            case REVISION: {
                Log.debug(getClass(), "Received git revision checkout event: " + name);

                final NotificationManager notificationManager = notificationManagerProvider.get();
                if (notificationManager != null) {
                    notificationManager.notify("Git operation", "Revision '" + name + "' is checked out", SUCCESS, EMERGE_MODE);
                }

                break;
            }
        }

        //Update project attributes from server.
        appContext.getWorkspaceRoot().synchronize();
    }
}
