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
package org.eclipse.che.api.core.model.machine;

/**
 * Provides description of the Che server in machine
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 */
public interface Server {
    /**
     * Reference to this Che server
     */
    String getRef();

    /**
     * Address of the server in form <b>host:port</b>
     */
    String getAddress();

    /**
     * Protocol of access to the server.
     */
    String getProtocol();

    /**
     * Path to access the server.
     */
    String getPath();

    /**
     * Url of the server, e.g. http://localhost:8080
     */
    String getUrl();
}
