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

import org.eclipse.che.api.agent.server.model.impl.AgentImpl;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;

import java.util.List;

/**
 * @author Sergii Leshchenko
 */
public interface BootstrapperFactory {
    Bootstrapper create(@Assisted String machineName,
                        @Assisted RuntimeIdentity runtimeIdentity,
                        @Assisted DockerMachine dockerMachine,
                        @Assisted List<AgentImpl> agents);
}
