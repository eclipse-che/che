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
package org.eclipse.che.ide.api.machine.execagent;

import com.google.inject.Inject;

import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventDto;
import org.eclipse.che.ide.jsonrpc.RequestHandlerConfigurator;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;

/**
 * Handles event fired by exec agent when process started
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class ProcessStartedEventHandler extends AbstractExecAgentEventHandler<ProcessStartedEventDto> {

    @Inject
    public void configureHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("process_started")
                    .paramsAsDto(ProcessStartedEventDto.class)
                    .noResult()
                    .withOperation(this);
    }

    @Override
    public void apply(String endpointId, ProcessStartedEventDto params) {
        Log.debug(getClass(), "Handling process started event. Params: " + params);
        handle(endpointId, params);
    }
}
