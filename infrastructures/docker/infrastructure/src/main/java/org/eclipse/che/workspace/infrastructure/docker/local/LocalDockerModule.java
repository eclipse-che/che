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
package org.eclipse.che.workspace.infrastructure.docker.local;

import static org.eclipse.che.workspace.infrastructure.docker.local.installer.LocalInstallersBinariesVolumeProvisioner.LOCAL_INSTALLERS_PROVISIONERS;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.eclipse.che.infrastructure.docker.client.DockerRegistryChecker;
import org.eclipse.che.workspace.infrastructure.docker.DockerEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.installer.ExecInstallerInfrastructureProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.installer.TerminalInstallerInfrastructureProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.installer.WsAgentBinariesInfrastructureProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.network.CheMasterExtraHostProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.network.CheMasterNetworkProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.projects.RemoveLocalProjectsFolderOnWorkspaceRemove;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/** @author Alexander Garagatyi */
public class LocalDockerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(RemoveLocalProjectsFolderOnWorkspaceRemove.class).asEagerSingleton();

    bind(DockerRegistryChecker.class).asEagerSingleton();

    bind(DockerEnvironmentProvisioner.class).to(LocalCheDockerEnvironmentProvisioner.class);

    Multibinder<ConfigurationProvisioner> localInstallersProvisioners =
        Multibinder.newSetBinder(
            binder(), ConfigurationProvisioner.class, Names.named(LOCAL_INSTALLERS_PROVISIONERS));
    localInstallersProvisioners.addBinding().to(ExecInstallerInfrastructureProvisioner.class);
    localInstallersProvisioners.addBinding().to(TerminalInstallerInfrastructureProvisioner.class);
    localInstallersProvisioners.addBinding().to(WsAgentBinariesInfrastructureProvisioner.class);

    Multibinder<ContainerSystemSettingsProvisioner> settingsProvisionerMB =
        Multibinder.newSetBinder(binder(), ContainerSystemSettingsProvisioner.class);
    settingsProvisionerMB.addBinding().to(CheMasterExtraHostProvisioner.class);
    settingsProvisionerMB.addBinding().to(CheMasterNetworkProvisioner.class);
  }
}
