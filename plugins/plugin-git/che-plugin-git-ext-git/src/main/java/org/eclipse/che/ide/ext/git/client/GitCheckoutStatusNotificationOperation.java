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
package org.eclipse.che.ide.ext.git.client;


import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestBiOperation;
import org.eclipse.che.ide.jsonrpc.RequestHandlerConfigurator;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Receives git checkout notifications caught by server side VFS file watching system.
 * Support two type of notifications: git branch checkout and git revision checkout.
 * After a notification is received it is processed and passed to and instance of
 * {@link NotificationManager}.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class GitCheckoutStatusNotificationOperation implements JsonRpcRequestBiOperation<GitCheckoutEventDto> {
    private final Provider<NotificationManager> notificationManagerProvider;

    @Inject
    public GitCheckoutStatusNotificationOperation(Provider<NotificationManager> notificationManagerProvider) {
        this.notificationManagerProvider = notificationManagerProvider;
    }

    @Inject
    public void configureHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("event:git-checkout")
                    .paramsAsDto(GitCheckoutEventDto.class)
                    .noResult()
                    .withOperation(this);
    }

    @Override
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
    }
}
