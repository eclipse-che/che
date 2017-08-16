/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.jsonrpc.commons.reception;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.slf4j.Logger;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Method configurator is used to define method name that the request handler
 * will be associated with.
 */
public class MethodNameConfigurator {
    private final static Logger LOGGER = getLogger(MethodNameConfigurator.class);

    private final RequestHandlerManager requestHandlerManager;

    @Inject
    MethodNameConfigurator(RequestHandlerManager requestHandlerManager) {
        this.requestHandlerManager = requestHandlerManager;
    }

    public ParamsConfigurator methodName(String name) {
        checkNotNull(name, "Method name must not be null");
        checkArgument(!name.isEmpty(), "Method name must not be empty");

        LOGGER.debug("Configuring incoming request method name name: " + name);

        return new ParamsConfigurator(requestHandlerManager, name);
    }
}
