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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;

/**
 * Modifies environment of workspace with everything needed for infrastructure of workspaces in CHE.
 *
 * @author Alexander Garagatyi
 */
public interface InfrastructureProvisioner {
    /**
     * Modifies environment config and internal environment representation with everything needed for infrastructure of workspace.
     *
     * @param envConfig
     *         configuration of environment
     * @param internalEnv
     *         internal environment representation
     * @throws EnvironmentException
     *         if any error occurs
     */
    void provision(Environment envConfig, CheServicesEnvironmentImpl internalEnv) throws EnvironmentException;

    /**
     * Modifies machine config and internal machine representation with everything needed for infrastructure of workspace.
     *
     * @param machineConfig
     *         configuration of machine
     * @param internalMachine
     *         internal machine representation
     * @throws EnvironmentException
     *         if any error occurs
     */
    void provision(ExtendedMachine machineConfig, CheServiceImpl internalMachine) throws EnvironmentException;
}
