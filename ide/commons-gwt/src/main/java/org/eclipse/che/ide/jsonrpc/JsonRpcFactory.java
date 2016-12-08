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

import com.google.inject.assistedinject.Assisted;

import java.util.List;

public interface JsonRpcFactory {
    JsonRpcRequest createRequest(@Assisted("message") String message);

    JsonRpcRequest createRequest(@Assisted("id") String id, @Assisted("method") String method, @Assisted("params") JsonRpcParams params);

    JsonRpcRequest createRequest(@Assisted("method") String method, @Assisted("params") JsonRpcParams params);

    JsonRpcResponse createResponse(@Assisted("message") String message);

    JsonRpcResponse createResponse(@Assisted("id") String id, @Assisted("result") JsonRpcResult result,
                                   @Assisted("error") JsonRpcError error);

    JsonRpcError createError(@Assisted("code") int code, @Assisted("message") String message);

    JsonRpcError createError(@Assisted("message") String message);

    JsonRpcResult createResult(@Assisted("message") String message);

    JsonRpcResult createResult(@Assisted("result") Object result);

    JsonRpcResult createResultList(@Assisted("result") List<?> result);

    JsonRpcList createList(@Assisted("message") String message);

    JsonRpcList createList(@Assisted("dtoObjectList") List<?> dtoObjectList);

    JsonRpcParams createParams(@Assisted("message") String message);

    JsonRpcParams createParams(@Assisted("params") Object params);

    JsonRpcParams createParamsList(@Assisted("params") List<?> params);
}
