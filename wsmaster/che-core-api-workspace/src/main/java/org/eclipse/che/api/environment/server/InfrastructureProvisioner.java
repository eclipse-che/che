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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;

/**
 * Modifies environment of workspace with everything needed for CHE infrastructure.
 *
 * @author Alexander Garagatyi
 */
public interface InfrastructureProvisioner {
    void provision(Environment envConfig, CheServicesEnvironmentImpl internalEnv) throws EnvironmentException;

    void provision(ExtendedMachine machineConfig, CheServiceImpl internalMachine) throws EnvironmentException;
}
