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
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.machine.shared.dto.MachineLogMessageDto;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.util.loging.Log;

// FIXME: spi ide
@Singleton
class EnvironmentOutputHandler {

    @Inject
    EnvironmentOutputHandler(RequestHandlerConfigurator configurator, EventBus eventBus) {
        configurator.newConfiguration()
                    .methodName("event:environment-output:message")
                    .paramsAsDto(MachineLogMessageDto.class)
                    .noResult()
                    .withBiConsumer((endpointId, log) -> {
                        Log.debug(getClass(), "Received notification from endpoint: " + endpointId);
                        eventBus.fireEvent(new EnvironmentOutputEvent(log.getContent(), log.getMachineName()));
                    });
    }
}
