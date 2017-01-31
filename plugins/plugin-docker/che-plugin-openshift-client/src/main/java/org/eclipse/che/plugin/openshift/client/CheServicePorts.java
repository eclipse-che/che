/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.openshift.client;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * Provides mapping between port and Che service name that is using it
 */
public final class CheServicePorts {
    private static final Map<Integer, String> CHE_SERVICE_PORTS = ImmutableMap.<Integer, String> builder().
            put(22, "sshd").
            put(4401, "wsagent").
            put(4403, "wsagent-jpda").
            put(4411, "terminal").
            put(8080, "tomcat").
            put(8000, "tomcat-jpda").
            put(9876, "codeserver").build();

    private CheServicePorts() {
    }

    public static Map<Integer, String> get() {
        return CHE_SERVICE_PORTS;
    }
}
