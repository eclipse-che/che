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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.hosts;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/**
 * Adds extra entries of /etc/hosts to {@link DockerContainerConfig}. Retrieves hosts entries for
 * docker machines from property. Property {@value PROPERTY} contains hosts entries separated by
 * coma "," sign.
 *
 * @author Alexander Garagatyi
 */
public class ExtraHostsProvisioner implements ContainerSystemSettingsProvisioner {
  private static final String PROPERTY = "che.workspace.hosts";

  private final List<String> extraHosts;

  @Inject
  public ExtraHostsProvisioner(@Nullable @Named(PROPERTY) String extraHosts) {
    if (extraHosts == null || extraHosts.isEmpty()) {
      this.extraHosts = Collections.emptyList();
    } else {
      this.extraHosts =
          Arrays.stream(extraHosts.split(" *, *"))
              .filter(host -> !host.isEmpty())
              .collect(toList());
    }
  }

  @Override
  public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
    for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
      containerConfig.getExtraHosts().addAll(extraHosts);
    }
  }
}
