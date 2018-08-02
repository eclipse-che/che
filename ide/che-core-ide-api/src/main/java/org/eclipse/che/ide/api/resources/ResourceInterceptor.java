/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.resources;

import com.google.common.annotations.Beta;

/**
 * Intercept resource which loads from the server and registers in internal client resource storage.
 *
 * <p>This interface is designed to modifying the specific resource before it would be cached. For
 * example any extension may create own implementation and set up specific markers for the given
 * resource or any other operations specific to the resource.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
@Beta
public interface ResourceInterceptor {

  /**
   * Intercepts given {@code resource} and returns it. Implementation is allowed to modify given
   * {@code resource}.
   *
   * @param resource the resource to intercept
   * @return the modified resource
   * @since 4.4.0
   */
  void intercept(Resource resource);

  /**
   * Default implementation of {@link ResourceInterceptor} which is do nothing except returning
   * given {@code resource}.
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
