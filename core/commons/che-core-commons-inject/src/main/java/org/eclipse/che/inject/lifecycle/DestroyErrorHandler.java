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
package org.eclipse.che.inject.lifecycle;

import java.lang.reflect.Method;

/**
 * Helps to be more flexible when need handle errors of invocation destroy-methods.
 *
 * @author andrew00x
 */
public interface DestroyErrorHandler {
    void onError(Object instance, Method method, Throwable error);

    /**
     * Implementation of DestroyErrorHandler that ignore errors, e.g. such behaviour is required for annotation {@link
     * javax.annotation.PreDestroy}.
     */
    DestroyErrorHandler DUMMY = new DestroyErrorHandler() {
        @Override
        public void onError(Object instance, Method method, Throwable error) {
        }
    };
}
