/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiEnvVarProvider;
import org.eclipse.che.infrastructure.docker.client.DockerRegistryDynamicAuthResolver;
import org.eclipse.che.infrastructure.docker.client.NoOpDockerRegistryDynamicAuthResolverImpl;
import org.eclipse.che.workspace.infrastructure.docker.bootstrap.DockerBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentsModule;
import org.eclipse.che.workspace.infrastructure.docker.environment.convert.DockerEnvironmentConvertersModule;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioningModule;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.cgroup.CGroupParentProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.dns.DnsSettingsProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.hosts.ExtraHostsProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.limits.cpu.CpuLimitsProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.limits.pids.PidLimitProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.limits.swap.SwapLimitProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.priviliged.PrivilegedModeProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.proxy.ProxySettingsProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.securityopt.SecurityOptProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.volume.ExtraVolumesProvisioner;

/** @author Alexander Garagatyi */
public class DockerInfraModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<ContainerSystemSettingsProvisioner> settingsProvisioners =
        Multibinder.newSetBinder(binder(), ContainerSystemSettingsProvisioner.class);
    settingsProvisioners.addBinding().to(DnsSettingsProvisioner.class);
    settingsProvisioners.addBinding().to(SecurityOptProvisioner.class);
    settingsProvisioners.addBinding().to(ExtraHostsProvisioner.class);
    settingsProvisioners.addBinding().to(ProxySettingsProvisioner.class);
    settingsProvisioners.addBinding().to(ExtraVolumesProvisioner.class);
    settingsProvisioners.addBinding().to(SwapLimitProvisioner.class);
    settingsProvisioners.addBinding().to(PidLimitProvisioner.class);
    settingsProvisioners.addBinding().to(CGroupParentProvisioner.class);
    settingsProvisioners.addBinding().to(CpuLimitsProvisioner.class);
    settingsProvisioners.addBinding().to(PrivilegedModeProvisioner.class);

    install(new DockerEnvironmentsModule());
    install(new DockerEnvironmentConvertersModule());
    install(new ContainerSystemSettingsProvisioningModule());

    bind(CheApiEnvVarProvider.class).to(DockerCheApiEnvVarProvider.class);

    bind(RuntimeInfrastructure.class).to(DockerRuntimeInfrastructure.class);

    bind(DockerRegistryDynamicAuthResolver.class)
        .to(NoOpDockerRegistryDynamicAuthResolverImpl.class);

    install(new FactoryModuleBuilder().build(DockerRuntimeFactory.class));
    install(new FactoryModuleBuilder().build(DockerBootstrapperFactory.class));
    install(new FactoryModuleBuilder().build(DockerRuntimeContextFactory.class));
    bind(
        org.eclipse.che.workspace.infrastructure.docker.monit.DockerAbandonedResourcesCleaner
            .class);
  }
}
