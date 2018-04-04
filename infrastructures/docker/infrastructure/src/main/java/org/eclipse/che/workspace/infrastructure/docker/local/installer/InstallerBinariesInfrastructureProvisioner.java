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
package org.eclipse.che.workspace.infrastructure.docker.local.installer;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.installer.server.impl.InstallerFqn;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.docker.WindowsHostUtils;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provisions environment with volumes needed for an installer.
 *
 * <p>On Windows MUST be locate in "user.home" directory in case limitation windows+docker.
 *
 * @author Alexander Garagatyi
 */
public abstract class InstallerBinariesInfrastructureProvisioner
    implements ConfigurationProvisioner {
  private static final Logger LOG =
      LoggerFactory.getLogger(InstallerBinariesInfrastructureProvisioner.class);

  private final String binariesVolume;
  private final String installerFqn;

  @Inject
  public InstallerBinariesInfrastructureProvisioner(
      String volumeOptions,
      String installerBinariesPath,
      String containerTarget,
      String installerFqn,
      String folderInHomeOnWindows) {
    this.installerFqn = installerFqn;

    volumeOptions = normalizeVolumeOptions(volumeOptions);
    if (SystemInfo.isWindows()) {
      installerBinariesPath = getWindowsPath(installerBinariesPath, folderInHomeOnWindows);
    }
    binariesVolume = getTargetOptions(installerBinariesPath, volumeOptions, containerTarget);
  }

  private String normalizeVolumeOptions(String volumeOptions) {
    if (!Strings.isNullOrEmpty(volumeOptions)) {
      return ":" + volumeOptions;
    } else {
      return "";
    }
  }

  private String getWindowsPath(String binariesLocation, String binariesLocationInHomeOnWindows) {
    try {
      Path cheHome = WindowsHostUtils.ensureCheHomeExist();
      Path path =
          Files.copy(
              Paths.get(binariesLocation),
              cheHome.resolve(binariesLocationInHomeOnWindows),
              REPLACE_EXISTING);
      return path.toString();
    } catch (IOException e) {
      LOG.warn(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private String getTargetOptions(String path, String agentVolumeOptions, String containerTarget) {
    return path + containerTarget + agentVolumeOptions;
  }

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (Map.Entry<String, InternalMachineConfig> machineConfigEntry :
        internalEnv.getMachines().entrySet()) {
      InternalMachineConfig machineConfig = machineConfigEntry.getValue();

      if (InstallerFqn.idInInstallerList(installerFqn, machineConfig.getInstallers())) {
        DockerContainerConfig containerConfig =
            internalEnv.getContainers().get(machineConfigEntry.getKey());
        containerConfig.getVolumes().add(binariesVolume);
      }
    }
  }
}
