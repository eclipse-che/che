/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.local;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.infrastructure.docker.client.DockerRegistryChecker;
import org.eclipse.che.workspace.infrastructure.docker.DockerEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.OnWorkspaceRemoveDataVolumeRemover;
import org.eclipse.che.workspace.infrastructure.docker.local.network.CheMasterExtraHostProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.network.CheMasterNetworkProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.projects.RemoveLocalProjectsFolderOnWorkspaceRemove;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/** @author Alexander Garagatyi */
public class LocalDockerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(RemoveLocalProjectsFolderOnWorkspaceRemove.class).asEagerSingleton();
    bind(OnWorkspaceRemoveDataVolumeRemover.class).asEagerSingleton();

    bind(DockerRegistryChecker.class).asEagerSingleton();

    bind(DockerEnvironmentProvisioner.class).to(LocalCheDockerEnvironmentProvisioner.class);

    Multibinder<ContainerSystemSettingsProvisioner> settingsProvisionerMB =
        Multibinder.newSetBinder(binder(), ContainerSystemSettingsProvisioner.class);
    settingsProvisionerMB.addBinding().to(CheMasterExtraHostProvisioner.class);
    settingsProvisionerMB.addBinding().to(CheMasterNetworkProvisioner.class);
  }
}
