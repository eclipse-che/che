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
package org.eclipse.che.ide.api.resources;

import com.google.common.annotations.Beta;

/**
 * Intercept resource which loads from the server and registers in internal client resource storage.
 * <p/>
 * This interface is designed to modifying the specific resource before it would be cached.
 * For example any extension may create own implementation and set up specific markers for the given
 * resource or any other operations specific to the resource.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
@Beta
public interface ResourceInterceptor {

    /**
     * Intercepts given {@code resource} and returns it. Implementation is allowed to modify given {@code resource}.
     *
     * @param resource
     *         the resource to intercept
     * @return the modified resource
     * @since 4.4.0
     */
    void intercept(Resource resource);

    /**
     * Default implementation of {@link ResourceInterceptor} which is do nothing except returning given {@code resource}.
     *
     * @see ResourceInterceptor
     * @since 4.4.0
     */
    class NoOpInterceptor implements ResourceInterceptor {

        /** {@inheritDoc} */
        @Override
        public void intercept(Resource resource) {
//            stub
        }
    }
}
