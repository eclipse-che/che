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
package org.eclipse.che.api.core.jsonrpc;

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Transmits json rpc requests containing DTO objects.
 *
 * Note: if you need to transmit or receive {@link String}, {@link Boolean},
 * {@link Double} use {@link BuildingRequestTransmitter} instance as it
 * provides more general facilities for that.
 */
@Singleton
public class RequestTransmitter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestTransmitter.class);

    private final BuildingRequestTransmitter transmitter;

    @Inject
    public RequestTransmitter(BuildingRequestTransmitter transmitter) {
        this.transmitter = transmitter;
    }

    private static <P> void checkParamsValue(P pValue) {
        checkNotNull(pValue, "Params value must not be null");
    }

    private static <P> void checkParamsValue(List<P> pValue) {
        checkNotNull(pValue, "Params value must not be null");
        checkArgument(!pValue.isEmpty(), "Params list must not be empty");
    }

    private static void checkMethodName(String method) {
        checkNotNull(method, "Method name must not be null");
        checkArgument(!method.isEmpty(), "Method name must not be empty");
    }

    private static void checkEndpointId(String endpointId) {
        checkNotNull(endpointId, "Endpoint ID must not be null");
        checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");
    }

    private static <R> void checkResultClass(Class<R> rClass) {
        checkNotNull(rClass, "Result class must not be null");
    }

    /**
     * Transmit a notification with empty params section
     *
     * @param endpointId
     *         endpoint to address a transmission
     * @param method
     *         method name to address a transmission
     */
    public void transmitNoneToNone(String endpointId, String method) {
        checkEndpointId(endpointId);
        checkMethodName(method);

        LOG.debug("Initiating a transmission of a notification: {}, method: {}", endpointId, method);

        transmitter.newRequest()
                   .endpointId(endpointId)
                   .methodName(method)
                   .paramsAsEmpty()
                   .sendAndSkipResult();
    }

    /**
     * Transmit a notification with params as a single object
     *
     * @param endpointId
     *         endpoint to address a transmission
     * @param method
     *         method name to address a transmission
     * @param pValue
     *         params value
     * @param <P>
     *         params class
     */
    public <P> void transmitOneToNone(String endpointId, String method, P pValue) {
        checkEndpointId(endpointId);
        checkMethodName(method);
        checkParamsValue(pValue);

        LOG.debug("Initiating a transmission of a notification:endpoint ID: {}, method: {}, params: {}", endpointId, method, pValue);

        transmitter.newRequest()
                   .endpointId(endpointId)
                   .methodName(method)
                   .paramsAsDto(pValue)
                   .sendAndSkipResult();

    }

    /**
     * Transmit a notification with params as a list of objects
     *
     * @param endpointId
     *         endpoint to address a transmission
     * @param method
     *         method name to address a transmission
     * @param pValue
     *         params value
     * @param <P>
     *         params class
     */
    public <P> void transmitManyToNone(String endpointId, String method, List<P> pValue) {
        checkEndpointId(endpointId);
        checkMethodName(method);
        checkParamsValue(pValue);

        LOG.debug("Initiating a transmission of a notification: endpoint ID: {}, method: {}, params list: {}", endpointId, method, pValue);

        transmitter.newRequest()
                   .endpointId(endpointId)
                   .methodName(method)
                   .paramsAsListOfDto(pValue)
                   .sendAndSkipResult();
    }

    /**
     * Transmit a request with an empty params section and a result as a single object
     *
     * @param endpointId
     *         endpoint to address a transmission
     * @param method
     *         method name to address a transmission
     * @param rClass
     *         result class
     * @param <R>
     *         result class
     *
     * @return
     */
    public <R> JsonRpcPromise<R> transmitNoneToOne(String endpointId, String method, Class<R> rClass) {
        checkEndpointId(endpointId);
        checkMethodName(method);
        checkResultClass(rClass);

        LOG.debug("Initiating a transmission of a request: endpoint ID: {}, method: {}, result class: {}", endpointId, method, rClass);

        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsEmpty()
                          .sendAndReceiveResultAsDto(rClass);
    }

    /**
     * Transmit a request with an empty params section and a result as a list of objects
     *
     * @param endpointId
     *         endpoint to address a transmission
     * @param method
     *         method name to address a transmission
     * @param rClass
     *         result class
     * @param <R>
     *         result class
     *
     * @return
     */
    public <R> JsonRpcPromise<List<R>> transmitNoneToMany(String endpointId, String method, Class<R> rClass) {
        checkEndpointId(endpointId);
        checkMethodName(method);
        checkResultClass(rClass);

        LOG.debug("Initiating a transmission of a request: endpoint ID: {}, method: {}, result list class: {}", endpointId, method, rClass);

        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsEmpty()
                          .sendAndReceiveResultAsListOfDto(rClass);
    }

    /**
     * Transmit a request with params as a single object and result as a single object
     *
     * @param endpointId
     *         endpoint to address a transmission
     * @param method
     *         method name to address a transmission
     * @param pValue
     *         params value
     * @param rClass
     *         result class
     * @param <P>
     *         params class
     * @param <R>
     *         result class
     *
     * @return
     */
    public <P, R> JsonRpcPromise<R> transmitOneToOne(String endpointId, String method, P pValue, Class<R> rClass) {
        checkEndpointId(endpointId);
        checkMethodName(method);
        checkParamsValue(pValue);
        checkResultClass(rClass);

        LOG.debug("Initiating a transmission of a request: " +
                  "endpoint ID: {}, method: {}, params object class: {}, params object value: {}, result object class: {}",
                  endpointId, method, pValue.getClass(), pValue, rClass);

        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsDto(pValue)
                          .sendAndReceiveResultAsDto(rClass);
    }

    /**
     * Transmit a request with params as s list of objects and result as a single object
     *
     * @param endpointId
     *         endpoint to address a transmission
     * @param method
     *         method name to address a transmission
     * @param pValue
     *         params value
     * @param rClass
     *         result class
     * @param <P>
     *         params class
     * @param <R>
     *         result class
     *
     * @return
     */
    public <P, R> JsonRpcPromise<R> transmitManyToOne(String endpointId, String method, List<P> pValue, Class<R> rClass) {
        checkEndpointId(endpointId);
        checkMethodName(method);
        checkParamsValue(pValue);
        checkResultClass(rClass);

        LOG.debug("Initiating a transmission of a request: " +
                  "endpoint ID: {}, method: {}, params list item class: {}, params list value: {}, result object class: {}",
                  endpointId, method, pValue.iterator().next().getClass(), pValue, rClass);

        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsListOfDto(pValue)
                          .sendAndReceiveResultAsDto(rClass);
    }

    /**
     * Transmit a request with params as a single object and result as a list of objects
     *
     * @param endpointId
     *         endpoint to address a transmission
     * @param method
     *         method name to address a transmission
     * @param pValue
     *         params value
     * @param rClass
     *         result class
     * @param <P>
     *         params class
     * @param <R>
     *         result class
     *
     * @return
     */
    public <P, R> JsonRpcPromise<List<R>> transmitOneToMany(String endpointId, String method, P pValue, Class<R> rClass) {
        checkEndpointId(endpointId);
        checkMethodName(method);
        checkParamsValue(pValue);
        checkResultClass(rClass);

        LOG.debug("Initiating a transmission of a request: " +
                  "endpoint ID: {}, method: {}, params object class: {}, params object value: {}, result list class: {}",
                  endpointId, method, pValue.getClass(), pValue, rClass);

        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsDto(pValue)
                          .sendAndReceiveResultAsListOfDto(rClass);
    }

    /**
     * Transmit a request with params as a list of objects and result as a list of objects
     *
     * @param endpointId
     *         endpoint to address a transmission
     * @param method
     *         method name to address a transmission
     * @param pValue
     *         params value
     * @param rClass
     *         result class
     * @param <P>
     *         params class
     * @param <R>
     *         result class
     *
     * @return
     */
    public <P, R> JsonRpcPromise<List<R>> transmitManyToMany(String endpointId, String method, List<P> pValue, Class<R> rClass) {
        checkEndpointId(endpointId);
        checkMethodName(method);
        checkResultClass(rClass);

        LOG.debug("Initiating a transmission of a request: " +
                  "endpoint ID: {}, method: {}, params list item class: {}, params list value: {}, result list class: {}",
                  endpointId, method, pValue.iterator().next().getClass(), pValue, rClass);

        return transmitter.newRequest()
                          .endpointId(endpointId)
                          .methodName(method)
                          .paramsAsListOfDto(pValue)
                          .sendAndReceiveResultAsListOfDto(rClass);
    }
}
