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

import org.eclipse.che.commons.annotation.Nullable;

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
     * External address of the server in form <b>hostname:port</b>.
     * <p>
     * This address is used by the browser to communicate with the server.
     * <b>hostname</b> can be configured using property machine.docker.local_node_host.external
     * or environment variable CHE_DOCKER_MACHINE_HOST_EXTERNAL.
     * <b>port</b> is the external port and cannot be configured.
     * If not explicitly configured that address is set using {@link ServerProperties#getInternalAddress()}
     */
    String getAddress();

    /**
     * Protocol of access to the server.
     */
    @Nullable
    String getProtocol();

    /**
     * Url of the server, e.g.&nbsp;http://localhost:8080
     */
    @Nullable
    String getUrl();


    /**
     * Non mandatory properties of the server.
     */
    @Nullable
    ServerProperties getProperties();
}
