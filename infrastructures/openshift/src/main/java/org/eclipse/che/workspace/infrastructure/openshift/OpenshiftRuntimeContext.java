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
package org.eclipse.che.workspace.infrastructure.openshift;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenshiftEnvironment;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.URI;

import static org.eclipse.che.api.workspace.server.OutputEndpoint.OUTPUT_WEBSOCKET_ENDPOINT_BASE;

/**
 * @author Sergii Leshchenko
 */
public class OpenshiftRuntimeContext extends RuntimeContext {
    private final OpenshiftEnvironment    openshiftEnvironment;
    private final OpenshiftRuntimeFactory runtimeFactory;
    private final String                  websocketEndpointBase;

    @Inject
    public OpenshiftRuntimeContext(@Assisted Environment environment,
                                   @Assisted OpenshiftEnvironment openshiftEnvironment,
                                   @Assisted RuntimeIdentity identity,
                                   @Assisted RuntimeInfrastructure infrastructure,
                                   InstallerRegistry installerRegistry,
                                   OpenshiftRuntimeFactory runtimeFactory,
                                   @Named("che.websocket.endpoint.base") String websocketEndpointBase)
            throws ValidationException,
                   InfrastructureException {
        super(environment, identity, infrastructure, installerRegistry);
        this.runtimeFactory = runtimeFactory;
        this.openshiftEnvironment = openshiftEnvironment;
        this.websocketEndpointBase = websocketEndpointBase;
    }

    @Override
    public URI getOutputChannel() throws InfrastructureException {
        try {
            return UriBuilder.fromUri(websocketEndpointBase)
                             .path(OUTPUT_WEBSOCKET_ENDPOINT_BASE)
                             .build();
        } catch (UriBuilderException | IllegalArgumentException ex) {
            throw new InternalInfrastructureException("Failed to get the output channel.  " +
                                                      ex.getMessage());
        }
    }

    @Override
    public InternalRuntime getRuntime() {
        return runtimeFactory.create(environment, openshiftEnvironment, identity, this);
    }
}
