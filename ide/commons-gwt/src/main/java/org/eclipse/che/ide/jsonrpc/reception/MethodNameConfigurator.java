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
package org.eclipse.che.ide.jsonrpc.reception;

import org.eclipse.che.ide.jsonrpc.JsonRpcFactory;
import org.eclipse.che.ide.jsonrpc.RequestHandlerRegistry;

import javax.inject.Inject;

/**
 * Method configurator is used to define method name that the request handler
 * will be associated with.
 */
public class MethodNameConfigurator {
    private final RequestHandlerRegistry registry;
    private final JsonRpcFactory         jsonRpcFactory;


    @Inject
    public MethodNameConfigurator(RequestHandlerRegistry registry, JsonRpcFactory jsonRpcFactory) {
        this.registry = registry;
        this.jsonRpcFactory = jsonRpcFactory;
    }

    public ParamsConfigurator methodName(String method) {
        return new ParamsConfigurator(registry, jsonRpcFactory, method);
    }


}
