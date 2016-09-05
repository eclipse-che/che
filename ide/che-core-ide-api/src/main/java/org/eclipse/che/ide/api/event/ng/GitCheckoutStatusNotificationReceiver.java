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
package org.eclipse.che.ide.api.event.ng;


import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class GitCheckoutStatusNotificationReceiver implements JsonRpcRequestReceiver {
    private final DtoFactory dtoFactory;

    private NotificationManager notificationManager;

    @Inject
    public GitCheckoutStatusNotificationReceiver(DtoFactory dtoFactory) {
        this.dtoFactory = dtoFactory;

    }

    public void inject(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    @Override
    public void receive(JsonRpcRequest request) {
        final String params = request.getParams();
        final GitCheckoutEventDto gitCheckoutEventDto = dtoFactory.createDtoFromJson(params, GitCheckoutEventDto.class);

        final Type type = gitCheckoutEventDto.getType();
        final String name = gitCheckoutEventDto.getName();

        switch (type) {
            case BRANCH: {
                Log.debug(getClass(), "Received git branch checkout event: " + name);

                if (notificationManager != null) {
                    notificationManager.notify("Git operation", "Branch '" + name + "' is checked out", SUCCESS, EMERGE_MODE);
                }

                break;
            }
            case REVISION: {
                Log.debug(getClass(), "Received git revision checkout event: " + name);

                if (notificationManager != null) {
                    notificationManager.notify("Git operation", "Revision '" + name + "' is checked out", SUCCESS, EMERGE_MODE);
                }

                break;
            }
        }
    }
}
