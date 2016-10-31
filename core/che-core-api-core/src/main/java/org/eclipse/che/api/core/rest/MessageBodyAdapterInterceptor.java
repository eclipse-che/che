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
package org.eclipse.che.api.core.rest;

import com.google.common.annotations.Beta;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This interceptor must be bound for the method
 * {@link MessageBodyReader#readFrom(Class, Type, Annotation[], MediaType, MultivaluedMap, InputStream)}
 *
 * @author Yevhenii Voevodin
 */
@Beta
public class MessageBodyAdapterInterceptor implements MethodInterceptor {

    private final Map<Class<?>, MessageBodyAdapter> adapters = new HashMap<>();

    @Inject
    public void init(Set<MessageBodyAdapter> adapters) {
        for (MessageBodyAdapter adapter : adapters) {
            for (Class<?> trigger : adapter.getTriggers()) {
                this.adapters.put(trigger, adapter);
            }
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Object[] args = invocation.getArguments();
        final MessageBodyAdapter adapter = adapters.get((Class<?>)args[0]);
        if (adapter != null) {
            args[args.length - 1] = adapter.adapt((InputStream)args[args.length - 1]);
        }
        return invocation.proceed();
    }
}
