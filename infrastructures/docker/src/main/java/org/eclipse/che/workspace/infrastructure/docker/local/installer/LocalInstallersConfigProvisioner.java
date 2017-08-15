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
package org.eclipse.che.workspace.infrastructure.docker.local.installer;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.installer.InstallerConfigApplier;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.installer.InstallersConfigProvisioner;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

/**
 * Provisions an environment with configuration and binaries that comes from installers of machines in the environment.
 *
 * @author Alexander Garagatyi
 */
public class LocalInstallersConfigProvisioner extends InstallersConfigProvisioner {
    public static final String LOCAL_INSTALLERS_PROVISIONERS =
            "infrastructure.docker.local_installers_provisioners";

    private final Set<ConfigurationProvisioner> localInstallerProvisioners;

    @Inject
    public LocalInstallersConfigProvisioner(
            InstallerConfigApplier installerConfigApplier,
            @Named(LOCAL_INSTALLERS_PROVISIONERS) Set<ConfigurationProvisioner> localInstallerProvisioners) {
        super(installerConfigApplier);
        this.localInstallerProvisioners = localInstallerProvisioners;
    }

    @Override
    public void provision(EnvironmentImpl envConfig, DockerEnvironment internalEnv, RuntimeIdentity identity)
            throws InfrastructureException {

        super.provision(envConfig, internalEnv, identity);
        for (ConfigurationProvisioner infrastructureProvisioner : localInstallerProvisioners) {
            infrastructureProvisioner.provision(envConfig, internalEnv, identity);
        }
    }
}
