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

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.shared.Utils;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;

/**
 * @author Alexander Garagatyi
 */
// TODO Check what if start fails and interruption called or stop called
// TODO interrupted exception, closedbyinteruptionexception

// TODO stop of starting WS - if not supported specific exception
// TODO stop add warning on errors?
// TODO stop in which cases to throw an exception?

// TODO exception on start
// TODO remove starting machine if present
// TODO Check if interruption came from stop or because of another reason
// TODO if because of another reason stop environment
public class DockerRuntimeContext extends RuntimeContext {
    private final DockerEnvironment    dockerEnvironment;
    private final DockerRuntimeFactory dockerInfraFactory;
    private final List<String>         orderedServices;
    private final String               devMachineName;

    @Inject
    public DockerRuntimeContext(@Assisted DockerRuntimeInfrastructure infrastructure,
                                @Assisted RuntimeIdentity identity,
                                @Assisted Environment environment,
                                @Assisted DockerEnvironment dockerEnvironment,
                                @Assisted List<String> orderedServices,
                                AgentSorter agentSorter,
                                AgentRegistry agentRegistry,
                                DockerRuntimeFactory dockerInfraFactory)
            throws ValidationException, InfrastructureException {
        super(environment, identity, infrastructure, agentSorter, agentRegistry);
        this.devMachineName = Utils.getDevMachineName(environment);
        this.orderedServices = orderedServices;
        this.dockerEnvironment = dockerEnvironment;
        this.dockerInfraFactory = dockerInfraFactory;
    }

    @Override
    public URL getOutputChannel() throws InfrastructureException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InternalRuntime getRuntime() {
        return dockerInfraFactory.createRuntime(this,
                                                devMachineName,
                                                orderedServices,
                                                dockerEnvironment,
                                                identity);
    }
}
