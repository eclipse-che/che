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

import java.util.Map;

/**
 * Describe metadata of machine.
 *
 * @author Alexander Garagatyi
 */
public interface MachineMetadata {
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
     * Returns mapping of exposed ports to {@link Server}
     */
    Map<String, ? extends Server> getServers();
}
