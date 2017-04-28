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
package org.eclipse.che.api.core.jsonrpc.commons.reception;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.logger.commons.Logger;
import org.eclipse.che.api.core.logger.commons.LoggerFactory;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Method configurator is used to define method name that the request handler
 * will be associated with.
 */
public class MethodNameConfigurator {
    private final Logger                logger;
    private final LoggerFactory         loggerFactory;
    private final RequestHandlerManager requestHandlerManager;

    @Inject
    MethodNameConfigurator(LoggerFactory loggerFactory, RequestHandlerManager requestHandlerManager) {
        this.logger = loggerFactory.get(getClass());
        this.loggerFactory = loggerFactory;
        this.requestHandlerManager = requestHandlerManager;
    }

    public ParamsConfigurator methodName(String name) {
        checkNotNull(name, "Method name must not be null");
        checkArgument(!name.isEmpty(), "Method name must not be empty");

        logger.debug("Configuring incoming request method name name: "+ name);

        return new ParamsConfigurator(loggerFactory, requestHandlerManager, name);
    }
}
