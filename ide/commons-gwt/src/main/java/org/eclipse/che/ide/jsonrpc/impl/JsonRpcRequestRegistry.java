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

import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Binds request identifiers with request methods. Registry is used by response receivers
 * to find out what method call is being answered. This is mostly needed because JSON RPC
 * specification does not define method section in JSON RPC responses so there is no method
 * name that can be directly mapped to a corresponding receiver.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class JsonRpcRequestRegistry {
    private final Map<Integer, String> requests = new HashMap<>();

    /**
     * Add request identifier - request method binding to the registry.
     *
     * @param id request identifier
     * @param method request method
     */
    public void add(Integer id, String method) {
        Log.debug(getClass(), "Binding ID: ", id, " to method: ", method);

        requests.put(id, method);
    }

    /**
     * Extracts request method name bound to request identifier
     *
     * @param id request identifier
     *
     * @return request method name
     */
    public String extractFor(Integer id) {
        Log.debug(getClass(), "Extracting method with ID: ", id);

        return requests.remove(id);
    }
}
