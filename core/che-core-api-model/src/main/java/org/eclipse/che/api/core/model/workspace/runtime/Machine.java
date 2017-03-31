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
package org.eclipse.che.api.core.model.workspace.runtime;

import org.eclipse.che.api.core.model.machine.OldServer;

import java.util.Map;

/**
 * Runtime information about machine.
 *
 * @author Alexander Garagatyi
 */
public interface Machine {

    /**
     * Returns machine specific properties.
     */
    Map<String, String> getProperties();


    /**
     * Returns mapping of exposed ports to {@link OldServer}.
     *
     * <p>Key consist of port number and transport protocol - tcp or udp with slash between these parts.
     * <br>Example:
     * <pre>
     * {
     *     8080/tcp : {
     *         "ref" : "server_reference",
     *         "address" : "server-with-machines.com",
     *         "url" : "http://server-with-machines.com:8080"
     *     }
     * }
     * </pre>
     */
    Map<String, ? extends Server> getServers();


}
