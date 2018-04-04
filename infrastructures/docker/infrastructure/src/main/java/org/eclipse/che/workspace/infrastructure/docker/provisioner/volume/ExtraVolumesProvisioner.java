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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.volume;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/**
 * Adds extra volumes to {@link DockerContainerConfig}. Provides volume configuration of machine for
 * any local directories that a user may want to mount into any docker machine. </br>{@code
 * che.workspace.volume} property is optional and contains semicolon separated extra volumes to
 * mount, for instance:
 *
 * <p>/path/on/host1:/path/in/container1;/path/on/host2:/path/in/container2
 *
 * @author Alexander Garagatyi
 * @author Anatolii Bazko
 */
@Singleton
public class ExtraVolumesProvisioner implements ContainerSystemSettingsProvisioner {

  private final Set<String> extraVolumes;

  @Inject
  public ExtraVolumesProvisioner(@Named("che.workspace.volume") String volume) {
    if (isNullOrEmpty(volume)) {
      this.extraVolumes = Collections.emptySet();
    } else {
      this.extraVolumes =
          Arrays.stream(volume.split(";")).filter(s -> !s.isEmpty()).collect(toSet());
    }
  }

  @Override
  public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
    for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
      containerConfig.getVolumes().addAll(extraVolumes);
    }
  }
}
