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
 * Not mandatory properties of a {@link Server}
 *
 * @author Mario Loriedo
 */
public interface ServerProperties {

    /**
     * Path to access the server.
     */
    @Nullable
    String getPath();

    /**
     * Internal address of the server in form <b>host:port</b>.
     * <p>
     * Used by wsmaster to communicate with the server
     */
    @Nullable
    String getInternalAddress();


    /**
     * Internal Url of the server, e.g.&nbsp;http://localhost:8080.
     * <p>
     * Used by wsmaster to comunicate with the server
     */
    @Nullable
    String getInternalUrl();
}
