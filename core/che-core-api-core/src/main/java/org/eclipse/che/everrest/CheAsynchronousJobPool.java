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
package org.eclipse.che.everrest;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.async.AsynchronousJob;
import org.everrest.core.impl.async.AsynchronousJobPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;


/** @author Vitaly Parfonov */
@Singleton
@Provider
public class CheAsynchronousJobPool extends AsynchronousJobPool implements ContextResolver<AsynchronousJobPool> {

    @Inject
    public CheAsynchronousJobPool(EverrestConfiguration everrestConfiguration) {
        super(everrestConfiguration);
    }

    @Override
    protected Callable<Object> newCallable(Object resource, Method method, Object[] params) {
        return ThreadLocalPropagateContext.wrap((super.newCallable(resource, method, params)));
    }
}
