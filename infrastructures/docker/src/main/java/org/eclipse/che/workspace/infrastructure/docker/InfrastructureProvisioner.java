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
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

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
     * @throws InfrastructureException
     *         if any error occurs
     */
    void provision(Environment envConfig, DockerEnvironment internalEnv) throws InfrastructureException;
}
