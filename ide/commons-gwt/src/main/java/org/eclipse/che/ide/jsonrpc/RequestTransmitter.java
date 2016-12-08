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
package org.eclipse.che.ide.jsonrpc;

import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;

import javax.inject.Inject;
import java.util.List;

@Singleton
public class RequestTransmitter {
    private final BuildingRequestTransmitter transmitter;

    @Inject
    public RequestTransmitter(BuildingRequestTransmitter transmitter) {
        this.transmitter = transmitter;
    }

    public void transmitNoneToNone(String endpointId, String method) {
        transmitter.newRequest()
                   .endpointId(endpointId)
                   .methodName(method)
                   .paramsAsEmpty()
                   .sendAndSkipResult();
    }

    public <P> void transmitOneToNone(String endpointId, String method, P pValue) {
        transmitter.newRequest()
                   .endpointId(endpointId)
                   .methodName(method)
                   .paramsAsDto(pValue)
                   .sendAndSkipResult();

    }

    public <P> void transmitManyToNone(String endpointId, String method, List<P> pValue) {
        transmitter.newRequest()
                   .endpointId(endpointId)
                   .methodName(method)
                   .paramsAsListOfDto(pValue)
                   .sendAndSkipResult();
    }

    public <R> Promise<R> transmitNoneToOne(String endpointId, String method, Class<R> rClass) {
        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsEmpty()
                          .sendAndReceiveResultAsDto(rClass);
    }

    public <R> Promise<List<R>> transmitNoneToMany(String endpointId, String method, Class<R> rClass) {
        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsEmpty()
                          .sendAndReceiveResultAsListOfDto(rClass);
    }

    public <P, R> Promise<R> transmitOneToOne(String endpointId, String method, P pValue, Class<R> rClass) {
        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsDto(pValue)
                          .sendAndReceiveResultAsDto(rClass);
    }

    public <P, R> Promise<R> transmitManyToOne(String endpointId, String method, List<P> pValue, Class<R> rClass) {
        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsListOfDto(pValue)
                          .sendAndReceiveResultAsDto(rClass);
    }

    public <P, R> Promise<List<R>> transmitOneToMany(String endpointId, String method, P pValue, Class<R> rClass) {
        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsDto(pValue)
                          .sendAndReceiveResultAsListOfDto(rClass);
    }

    public <P, R> Promise<List<R>> transmitManyToMany(String endpointId, String method, List<P> pValue, Class<R> rClass) {
        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsListOfDto(pValue)
                          .sendAndReceiveResultAsListOfDto(rClass);
    }
}
