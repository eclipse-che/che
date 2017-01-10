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
package org.eclipse.che.api.core.rest;

import javax.ws.rs.core.UriBuilder;

/**
 * Helps to deliver context of RESTful request to components.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public interface ServiceContext {
    /**
     * Get UriBuilder which already contains base URI of RESTful application and URL pattern of RESTful service that produces this
     * instance.
     */
    UriBuilder getServiceUriBuilder();

    /** Get UriBuilder which already contains base URI of RESTful application. */
    UriBuilder getBaseUriBuilder();
}
