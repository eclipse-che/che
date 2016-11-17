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


import org.eclipse.che.api.promises.client.Promise;

import java.util.List;

/**
 * Transmits requests to a defined endpoint over json rpc 2.0. protocol. Single
 * instance of transmitter is used for all registered endpoints. As json rpc is
 * a transport agnostic protocol the way the transmission goes is defined by
 * inner implementation.
 *
 * @author Dmitry Kuleshov
 */
public interface RequestTransmitter {
    /**
     * Transmit a notification that has no parameters to an endpoint
     *
     * @param endpointId
     *         high level endpoint identifier (e.g. "exec-agent")
     * @param method
     *         method name as defined in json rpc 2.0 specification
     */
    void transmitNotification(String endpointId, String method);

    /**
     * Transmit a notification that has parameters to an endpoint
     *
     * @param endpointId
     *         high level endpoint identifier (e.g. "exec-agent")
     * @param method
     *         method name as defined in json rpc 2.0 specification
     * @param params
     *         dto representing parameters
     */
    void transmitNotification(String endpointId, String method, Object params);

    /**
     * Transmit a request that has no parameters
     *
     * @param endpointId
     *         high level endpoint identifier (e.g. "exec-agent")
     * @param method
     *         method name as defined in json rpc 2.0 specification
     * @param resultClass
     *         class of response result section represented by DTO
     *
     * @return promise that contains response result represented by DTO
     */
    <T> Promise<T> transmitRequest(String endpointId, String method, Class<T> resultClass);

    /**
     * Transmit a request that has no parameters
     *
     * @param endpointId
     *         high level endpoint identifier (e.g. "exec-agent")
     * @param method
     *         method name as defined in json rpc 2.0 specification
     * @param params
     *         parameters represented by DTO
     * @param resultClass
     *         class of response result section represented by DTO
     *
     * @return promise that contains response result represented by DTO
     */
    <T> Promise<T> transmitRequest(String endpointId, String method, Object params, Class<T> resultClass);

    /**
     * Transmit a request that has no parameters and the result is a list of
     * objects represented by a corresponding DTO class.
     *
     * @param endpointId
     *         high level endpoint identifier (e.g. "exec-agent")
     * @param method
     *         method name as defined in json rpc 2.0 specification
     * @param resultClass
     *         class of response result section represented by DTO
     *
     * @return promise that contains response result represented by DTO
     */
    <T> Promise<List<T>> transmitRequestForList(String endpointId, String method, Class<T> resultClass);

    /**
     * Transmit a request that has parameters and the result is a list of
     * objects represented by a corresponding DTO class.
     *
     * @param endpointId
     *         high level endpoint identifier (e.g. "exec-agent")
     * @param method
     *         method name as defined in json rpc 2.0 specification
     * @param params
     *         parameters represented by DTO
     * @param resultClass
     *         class of response result section represented by DTO
     *
     * @return promise that contains response result represented by DTO
     */
    <T> Promise<List<T>> transmitRequestForList(String endpointId, String method, Object params, Class<T> resultClass);
}
