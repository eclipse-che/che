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

import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenshiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenshiftEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenshiftEnvironmentProvisioner;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Sergii Leshchenko
 */
@Singleton
public class OpenshiftInfrastructure extends RuntimeInfrastructure {
    private final OpenshiftRuntimeContextFactory  runtimeContextFactory;
    private final OpenshiftEnvironmentProvisioner envProvisioner;
    private final OpenshiftEnvironmentParser      envParser;

    @Inject
    public OpenshiftInfrastructure(EventService eventService,
                                   OpenshiftRuntimeContextFactory runtimeContextFactory,
                                   OpenshiftEnvironmentParser envParser,
                                   OpenshiftEnvironmentProvisioner envProvisioner) {
        super("openshift", ImmutableSet.of("openshift"), eventService);
        this.runtimeContextFactory = runtimeContextFactory;
        this.envParser = envParser;
        this.envProvisioner = envProvisioner;
    }

    @Override
    public Environment estimate(Environment environment) throws ValidationException, InfrastructureException {
        //TODO implement estimation and validation here
        return environment;
    }

    @Override
    public RuntimeContext prepare(RuntimeIdentity id, Environment environment) throws ValidationException,
                                                                                      InfrastructureException {
        OpenshiftEnvironment openshiftEnvironment = envParser.parse(environment);
        envProvisioner.provision(environment, openshiftEnvironment, id);

        return runtimeContextFactory.create(environment,
                                            openshiftEnvironment,
                                            id,
                                            this);
    }
}
