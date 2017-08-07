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
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.openshift.provision.InstallerConfigApplier;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Sergii Leshchenko
 */
@Singleton
public class OpenShiftInfrastructure extends RuntimeInfrastructure {
    private final OpenShiftRuntimeContextFactory runtimeContextFactory;
    private final InstallerConfigApplier         installerConfigApplier;
    private final OpenShiftEnvironmentParser     envParser;

    @Inject
    public OpenShiftInfrastructure(EventService eventService,
                                   OpenShiftRuntimeContextFactory runtimeContextFactory,
                                   OpenShiftEnvironmentParser envParser,
                                   InstallerConfigApplier installerConfigApplier) {
        super("openshift", ImmutableSet.of("openshift"), eventService);
        this.runtimeContextFactory = runtimeContextFactory;
        this.envParser = envParser;
        this.installerConfigApplier = installerConfigApplier;
    }

    @Override
    public Environment estimate(Environment environment) throws ValidationException, InfrastructureException {
        //TODO implement estimation and validation here
        return environment;
    }

    @Override
    public RuntimeContext prepare(RuntimeIdentity id, Environment environment) throws ValidationException,
                                                                                      InfrastructureException {
        OpenShiftEnvironment openShiftEnvironment = envParser.parse(environment);

        installerConfigApplier.apply(id, environment, openShiftEnvironment);

        return runtimeContextFactory.create(environment,
                                            openShiftEnvironment,
                                            id,
                                            this);
    }
}
