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

/**
 * HTTP output message.
 *
 * @author andrew00x
 */
public interface HttpOutputMessage extends OutputProvider {
    /** Set HTTP status. */
    void setStatus(int status);

    /** Shortcut to set content-type header. The same may be none with method {@link #setHttpHeader(String, String)}. */
    void setContentType(String contentType);

    /**
     * Add HTTP header.
     *
     * @param name
     *         name of header
     * @param value
     *         value of header
     */
    void addHttpHeader(String name, String value);

    /**
     * Set HTTP header. If the header had already been set, the new value overwrites the previous one.
     *
     * @param name
     *         name of header
     * @param value
     *         value of header
     */
    void setHttpHeader(String name, String value);
}
