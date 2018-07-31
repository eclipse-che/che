/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest;

import javax.ws.rs.core.UriBuilder;

/**
 * Helps to deliver context of RESTful request to components.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public interface ServiceContext {
  /**
   * Get UriBuilder which already contains base URI of RESTful application and URL pattern of
   * RESTful service that produces this instance.
   */
  UriBuilder getServiceUriBuilder();

  /** Get UriBuilder which already contains base URI of RESTful application. */
  UriBuilder getBaseUriBuilder();
}
