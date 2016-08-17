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
package org.eclipse.che.ide.jsonrpc.impl;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;

/**
 * There are two implementations of this interface:
 *
 * <ul>
 * <li>{@link WebSocketJsonRpcRequestDispatcher}</li>
 * <li>{@link WebSocketJsonRpcResponseDispatcher}</li>
 * </ul>
 *
 * Each implementation is used to dispatch messages of {@link JsonRpcObject}
 * of corresponding type: requests or response.
 *
 * @author Dmitry Kuleshov
 */
public interface JsonRpcDispatcher {
    /**
     * Dispatches a message
     *
     * @param message
     *         message
     */
    void dispatch(String message);
}
