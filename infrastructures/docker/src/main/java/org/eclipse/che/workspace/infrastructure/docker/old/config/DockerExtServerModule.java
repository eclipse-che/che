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
package org.eclipse.che.workspace.infrastructure.docker.old.config;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Guice module for extension servers feature in docker machines
 *
 * @author Alexander Garagatyi
 * @author Sergii Leschenko
 * @author Roman Iuvshyn
 */
// Not a DynaModule, install manually
public class DockerExtServerModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<String> devMachineEnvVars = Multibinder.newSetBinder(binder(),
                                                                         String.class,
                                                                         Names.named("machine.docker.dev_machine.machine_env"))
                                                           .permitDuplicates();
        devMachineEnvVars.addBinding().toProvider(
                org.eclipse.che.workspace.infrastructure.docker.old.config.provider.ProjectsRootEnvVariableProvider.class);
        devMachineEnvVars.addBinding().toProvider(
                org.eclipse.che.workspace.infrastructure.docker.old.config.provider.JavaOptsEnvVariableProvider.class);
    }
}
