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

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import java.util.List;

/**
 * @author Alexander Garagatyi
 */
public interface RuntimeFactory {
    DockerRuntimeContext createContext(@Assisted DockerRuntimeInfrastructure infrastructure,
                                       @Assisted RuntimeIdentity identity,
                                       @Assisted Environment environmentConfig,
                                       @Assisted DockerEnvironment dockerEnvironment,
                                       @Assisted List<String> servicesOrder)
            throws ValidationException, InfrastructureException;
}
