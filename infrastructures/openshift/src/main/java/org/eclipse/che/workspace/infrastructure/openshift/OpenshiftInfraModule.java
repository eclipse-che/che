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

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.openshift.bootstrapper.OpenshiftBootstrapperFactory;

/**
 * @author Sergii Leshchenko
 */
public class OpenshiftInfraModule extends AbstractModule {
    @Override
    protected void configure() {

        Multibinder<RuntimeInfrastructure> infrastructures = Multibinder.newSetBinder(binder(),
                                                                                      RuntimeInfrastructure.class);
        infrastructures.addBinding().to(OpenshiftInfrastructure.class);

//      TODO Revise bind(WorkspaceFilesCleaner.class).toInstance(workspace -> {});

        install(new FactoryModuleBuilder().build(OpenshiftRuntimeContextFactory.class));
        install(new FactoryModuleBuilder().build(OpenshiftRuntimeFactory.class));
        install(new FactoryModuleBuilder().build(OpenshiftBootstrapperFactory.class));
    }
}
