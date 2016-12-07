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
package org.eclipse.che.ide.jsonrpc;

import javax.inject.Singleton;
import java.util.Map;

/**
 * Performs all needed preparations to initialize and terminate the json rpc
 * service.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public interface JsonRpcInitializer {
    /**
     * Initialize json rpc service for an endpoint defined by a high level
     * identifier with implementation defined properties.
     *
     * @param endpointId
     *         high level endpoint identifier (e.g. "exec-agent")
     * @param properties
     *         map of implementation dependent properties (e.g. URL, etc.)
     */
    void initialize(String endpointId, Map<String, String> properties);

    /**
     * Terminate json rpc service defined by high level identifier
     *
     * @param endpointId
     *         high level endpoint identifier (e.g. "exec-agent")
     */
    void terminate(String endpointId);
}
