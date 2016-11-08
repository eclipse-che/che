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
package org.eclipse.che.api.core.jsonrpc;


import java.util.concurrent.CompletableFuture;

/**
 * @author Dmitry Kuleshov
 */
public interface RequestTransmitter {
    void transmit(String endpointId, String method);

    void transmit(String endpointId, String method, Object dto);

    <R> CompletableFuture<R> transmit(String endpointId, String method, Class<R> resultClass);

    <R> CompletableFuture<R> transmit(String endpointId, String method, Object dto, Class<R> resultClass);

    void transmit(String method, Object dto);
}
