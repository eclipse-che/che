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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import io.fabric8.kubernetes.api.model.EnvVar;

import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenshiftEnvironment;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Sergii Leshchenko
 */
public class OpenshiftEnvironmentProvisioner {
    private final String cheServerEndpoint;

    @Inject
    public OpenshiftEnvironmentProvisioner(@Named("che.infra.openshift.che_server_endpoint") String cheServerEndpoint) {
        this.cheServerEndpoint = cheServerEndpoint;
    }

    public void provision(Environment envConfig,
                          OpenshiftEnvironment internalEnv,
                          RuntimeIdentity identity) throws InfrastructureException {
        //TODO Add required ports to service(or create new one) and routes for agents

        EnvVar workspaceIdEnv = new EnvVar("CHE_WORKSPACE_ID", identity.getWorkspaceId(), null);
        EnvVar cheApiEnv = new EnvVar("CHE_API", cheServerEndpoint, null);

        internalEnv.getPods()
                   .values()
                   .stream()
                   .flatMap(p -> p.getSpec().getContainers().stream())
                   .forEach(c -> {
                       c.getEnv().removeIf(e -> e.getName().equals("CHE_WORKSPACE_ID") || e.getName().equals("CHE_API"));

                       c.getEnv().add(workspaceIdEnv);
                       c.getEnv().add(cheApiEnv);
                   });
    }
}
