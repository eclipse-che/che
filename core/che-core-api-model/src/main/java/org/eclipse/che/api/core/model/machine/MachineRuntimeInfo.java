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
package org.eclipse.che.api.core.model.machine;

import java.util.Map;

/**
 * Runtime information about machine.
 *
 * @author Alexander Garagatyi
 */
public interface MachineRuntimeInfo {
    /**
     * Returns environment variables of machine.
     */
    Map<String, String> getEnvVariables();

    /**
     * Returns machine specific properties.
     */
    Map<String, String> getProperties();

    /**
     * It is supposed that this methods returns the same as {@code getEnvVariables().get("CHE_PROJECTS_ROOT")}.
     */
    String projectsRoot();

    /**
     * Returns mapping of exposed ports to {@link Server}.
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
