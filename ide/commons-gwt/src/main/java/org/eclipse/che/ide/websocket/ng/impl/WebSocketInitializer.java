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
package org.eclipse.che.ide.websocket.ng.impl;

import javax.inject.Singleton;
import java.util.Map;

/**
 * Initializes and terminates WEB SOCKET services.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public interface WebSocketInitializer {
    /**
     * Initialize WEB SOCKET services with properties contained
     * in a map as simple key->value pairs.
     *
     * @param properties properties map
     */
    void initialize(Map<String, String> properties);

    /**
     * Terminates WEB SOCKET services
     */
    void terminate();
}
