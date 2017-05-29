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
package org.eclipse.che.ide.workspace.events;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.ide.machine.EnvironmentStatusHandler;
import org.eclipse.che.ide.util.loging.Log;

@Singleton
class EnvironmentStatusEventHandler {

    @Inject
    void configureEnvironmentStatusHandler(RequestHandlerConfigurator configurator, Provider<EnvironmentStatusHandler> handlerProvider) {
        configurator.newConfiguration()
                    .methodName("event:environment-status:changed")
                    .paramsAsDto(MachineStatusEvent.class)
                    .noResult()
                    .withBiConsumer((endpointId, event) -> {
                        Log.debug(getClass(), "Received notification from endpoint: " + endpointId);

                        // Since EnvironmentStatusEventHandler instantiated by GIN eagerly,
                        // it may be really expensive to instantiate EnvironmentStatusHandler with all it's dependencies.
                        // So defer that work.
                        handlerProvider.get().handleEnvironmentStatusChanged(event);
                    });
    }
}
