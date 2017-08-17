/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.openshift;

import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentParser;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Sergii Leshchenko
 */
@Singleton
public class OpenShiftInfrastructure extends RuntimeInfrastructure {
    private final OpenShiftRuntimeContextFactory runtimeContextFactory;
    private final OpenShiftEnvironmentParser     envParser;
    private final OpenShiftInfrastructureProvisioner      infrastructureProvisioner;

    @Inject
    public OpenShiftInfrastructure(EventService eventService,
                                   OpenShiftRuntimeContextFactory runtimeContextFactory,
                                   OpenShiftEnvironmentParser envParser,
                                   OpenShiftInfrastructureProvisioner infrastructureProvisioner) {
        super("openshift", ImmutableSet.of("openshift"), eventService);
        this.runtimeContextFactory = runtimeContextFactory;
        this.envParser = envParser;
        this.infrastructureProvisioner = infrastructureProvisioner;
    }

    @Override
    public Environment estimate(Environment environment) throws ValidationException, InfrastructureException {
        //TODO implement estimation and validation here
        return environment;
    }

    @Override
    public RuntimeContext prepare(RuntimeIdentity id, Environment originEnv) throws ValidationException,
                                                                                    InfrastructureException {
        final EnvironmentImpl environment = new EnvironmentImpl(originEnv);
        final OpenShiftEnvironment openShiftEnvironment = envParser.parse(environment);

        infrastructureProvisioner.provision(environment, openShiftEnvironment, id);

        return runtimeContextFactory.create(environment,
                                            openShiftEnvironment,
                                            id,
                                            this);
    }
}
