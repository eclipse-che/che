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

import static org.eclipse.che.workspace.infrastructure.openshift.project.pvc.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.openshift.project.pvc.UniqueWorkspacePVCStrategy.UNIQUE_STRATEGY;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.openshift.bootstrapper.OpenShiftBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.openshift.project.RemoveProjectOnWorkspaceRemove;
import org.eclipse.che.workspace.infrastructure.openshift.project.pvc.CommonPVCStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.project.pvc.UniqueWorkspacePVCStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.project.pvc.WorkspacePVCCleaner;
import org.eclipse.che.workspace.infrastructure.openshift.project.pvc.WorkspaceVolumeStrategyProvider;
import org.eclipse.che.workspace.infrastructure.openshift.project.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftCheApiEnvVarProvider;

/** @author Sergii Leshchenko */
public class OpenShiftInfraModule extends AbstractModule {
  @Override
  protected void configure() {
    MapBinder<String, InternalEnvironmentFactory> factories =
        MapBinder.newMapBinder(binder(), String.class, InternalEnvironmentFactory.class);

    factories.addBinding(OpenShiftEnvironment.TYPE).to(OpenShiftEnvironmentFactory.class);

    Multibinder<RuntimeInfrastructure> infrastructures =
        Multibinder.newSetBinder(binder(), RuntimeInfrastructure.class);
    infrastructures.addBinding().to(OpenShiftInfrastructure.class);

    install(new FactoryModuleBuilder().build(OpenShiftRuntimeContextFactory.class));
    install(new FactoryModuleBuilder().build(OpenShiftRuntimeFactory.class));
    install(new FactoryModuleBuilder().build(OpenShiftBootstrapperFactory.class));
    bind(WorkspacePVCCleaner.class).asEagerSingleton();
    bind(RemoveProjectOnWorkspaceRemove.class).asEagerSingleton();

    bind(CheApiEnvVarProvider.class).to(OpenShiftCheApiEnvVarProvider.class);

    MapBinder<String, WorkspaceVolumesStrategy> volumesStrategies =
        MapBinder.newMapBinder(binder(), String.class, WorkspaceVolumesStrategy.class);
    volumesStrategies.addBinding(COMMON_STRATEGY).to(CommonPVCStrategy.class);
    volumesStrategies.addBinding(UNIQUE_STRATEGY).to(UniqueWorkspacePVCStrategy.class);
    bind(WorkspaceVolumesStrategy.class).toProvider(WorkspaceVolumeStrategyProvider.class);
  }
}
