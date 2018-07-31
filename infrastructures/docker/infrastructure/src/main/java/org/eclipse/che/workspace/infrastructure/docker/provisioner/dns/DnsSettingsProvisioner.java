/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.provisioner.dns;

import javax.inject.Inject;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/**
 * Adds DNS configuration to {@link DockerContainerConfig}.
 *
 * @author Alexander Garagatyi
 */
public class DnsSettingsProvisioner implements ContainerSystemSettingsProvisioner {
  private DnsResolversProvider dnsResolversProvider;

  @Inject
  public DnsSettingsProvisioner(DnsResolversProvider dnsResolversProvider) {
    this.dnsResolversProvider = dnsResolversProvider;
  }

  @Override
  public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
    for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
      containerConfig.getDns().addAll(dnsResolversProvider.get());
    }
  }
}
