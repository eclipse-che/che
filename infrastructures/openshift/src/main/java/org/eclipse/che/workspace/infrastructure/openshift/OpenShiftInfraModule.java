/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import static org.eclipse.che.workspace.infrastructure.openshift.project.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.openshift.project.UniqueWorkspacePVCStrategy.UNIQUE_STRATEGY;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.openshift.bootstrapper.OpenShiftBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.openshift.project.CommonPVCStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.project.RemoveProjectOnWorkspaceRemove;
import org.eclipse.che.workspace.infrastructure.openshift.project.UniqueWorkspacePVCStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.project.WorkspacePVCCleaner;
import org.eclipse.che.workspace.infrastructure.openshift.project.WorkspacePVCStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.project.WorkspacePVCStrategyProvider;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftCheApiEnvVarProvider;

/** @author Sergii Leshchenko */
public class OpenShiftInfraModule extends AbstractModule {
  @Override
  protected void configure() {

    Multibinder<RuntimeInfrastructure> infrastructures =
        Multibinder.newSetBinder(binder(), RuntimeInfrastructure.class);
    infrastructures.addBinding().to(OpenShiftInfrastructure.class);

    install(new FactoryModuleBuilder().build(OpenShiftRuntimeContextFactory.class));
    install(new FactoryModuleBuilder().build(OpenShiftRuntimeFactory.class));
    install(new FactoryModuleBuilder().build(OpenShiftBootstrapperFactory.class));
    bind(WorkspacePVCCleaner.class).asEagerSingleton();
    bind(RemoveProjectOnWorkspaceRemove.class).asEagerSingleton();

    bind(CheApiEnvVarProvider.class).to(OpenShiftCheApiEnvVarProvider.class);

    MapBinder<String, WorkspacePVCStrategy> pvcStrategies =
        MapBinder.newMapBinder(binder(), String.class, WorkspacePVCStrategy.class);
    pvcStrategies.addBinding(COMMON_STRATEGY).to(CommonPVCStrategy.class);
    pvcStrategies.addBinding(UNIQUE_STRATEGY).to(UniqueWorkspacePVCStrategy.class);
    bind(WorkspacePVCStrategy.class).toProvider(WorkspacePVCStrategyProvider.class);
  }
}
