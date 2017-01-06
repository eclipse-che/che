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
package org.eclipse.che.api.core.model.workspace;

import java.util.Map;

/**
 * Configuration of server that can be started inside of machine.
 *
 * @author Alexander Garagatyi
 */
public interface ServerConf2 {
    /**
     * Port used by server.
     * <p>
     * If udp transport protocol is used then port should include /udp.<br>
     * If tcp is used /tcp is not required.<br>
     * Example:
     * <ul>
     *     <li>8080</li>
     *     <li>8080/tcp</li>
     *     <li>8080/udp</li>
     * </ul>
     */
    String getPort();

    /**
     * Protocol for configuring preview url of this server.
     * <p>
     * Example:
     * <ul>
     *     <li>http</li>
     *     <li>https</li>
     *     <li>tcp</li>
     *     <li>udp</li>
     *     <li>ws</li>
     *     <li>wss</li>
     * </ul>
     */
    String getProtocol();

    /**
     * Additional configuration that can be used to improve usage of machine servers.
     */
    Map<String, String> getProperties();
}
