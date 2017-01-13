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
package org.eclipse.che.api.workspace.shared;

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;

import java.util.List;
import java.util.Map;

/**
 * Utility class for workspace related code that might be useful on server or GWT client.
 *
 * @author Alexander Garagatyi
 */
public class Utils {
    private Utils() {}

    /**
     * Finds dev machine name in environment definition.
     *
     * @param envConfig
     *         environment definition
     * @return dev machine name or {@code null} if dev machine is not found in environment
     */
    public static String getDevMachineName(Environment envConfig) {
        for (Map.Entry<String, ? extends ExtendedMachine> entry : envConfig.getMachines().entrySet()) {
            List<String> agents = entry.getValue().getAgents();
            if (agents != null && agents.contains("org.eclipse.che.ws-agent")) {
                return entry.getKey();
            }
        }
        return null;
    }
}
