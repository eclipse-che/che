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
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.execagent.event.ConnectedEventDto;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestBiOperation;
import org.eclipse.che.ide.jsonrpc.RequestHandlerConfigurator;

/**
 * Handles 'connected' event, the event is fired when we firstly connect to exec agent.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class ConnectedEventHandler implements JsonRpcRequestBiOperation<ConnectedEventDto> {

    @Inject
    public void configureHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("connected")
                    .paramsAsDto(ConnectedEventDto.class)
                    .noResult()
                    .withOperation(this);
    }

    @Override
    public void apply(String endpointId, ConnectedEventDto params) {

    }
}
