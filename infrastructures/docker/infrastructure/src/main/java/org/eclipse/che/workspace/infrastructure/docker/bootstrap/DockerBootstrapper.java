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
package org.eclipse.che.workspace.infrastructure.docker.bootstrap;

import com.google.gson.Gson;
import com.google.inject.assistedinject.Assisted;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.bootstrap.AbstractBootstrapper;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.lang.TarUtils;
import org.eclipse.che.workspace.infrastructure.docker.DockerMachine;

/**
 * Bootstraps installers in docker machines.
 *
 * @author Sergii Leshchenko
 */
public class DockerBootstrapper extends AbstractBootstrapper {
  private static final Gson GSON = new Gson();

  private static final String BOOTSTRAPPER_BASE_DIR = "/tmp/";
  private static final String BOOTSTRAPPER_DIR = BOOTSTRAPPER_BASE_DIR + "bootstrapper/";
  private static final String BOOTSTRAPPER_FILE = "bootstrapper";
  private static final String CONFIG_FILE = "config.json";

  private final String machineName;
  private final RuntimeIdentity runtimeIdentity;
  private final DockerMachine dockerMachine;
  private final List<Installer> installers;
  private final int serverCheckPeriodSeconds;
  private final int installerTimeoutSeconds;

  @Inject
  public DockerBootstrapper(
      @Assisted String machineName,
      @Assisted RuntimeIdentity runtimeIdentity,
      @Assisted DockerMachine dockerMachine,
      @Assisted List<Installer> installers,
      EventService eventService,
      @Named("che.infra.docker.master_websocket_endpoint") String cheWebsocketEndpoint,
      @Named("che.infra.docker.bootstrapper.timeout_min") int bootstrappingTimeoutMinutes,
      @Named("che.infra.docker.bootstrapper.installer_timeout_sec") int installerTimeoutSeconds,
      @Named("che.infra.docker.bootstrapper.server_check_period_sec")
          int serverCheckPeriodSeconds) {
    super(
        machineName,
        runtimeIdentity,
        bootstrappingTimeoutMinutes,
        cheWebsocketEndpoint,
        cheWebsocketEndpoint,
        eventService);
    this.machineName = machineName;
    this.runtimeIdentity = runtimeIdentity;
    this.dockerMachine = dockerMachine;

    this.installers = installers;
    this.serverCheckPeriodSeconds = serverCheckPeriodSeconds;
    this.installerTimeoutSeconds = installerTimeoutSeconds;
  }

  @Override
  protected void doBootstrapAsync(String installerWebsocketEndpoint, String outputWebsocketEndpoint)
      throws InfrastructureException {
    injectBootstrapper();

    dockerMachine.exec(
        BOOTSTRAPPER_DIR
            + BOOTSTRAPPER_FILE
            + " -machine-name "
            + machineName
            + " -runtime-id "
            + String.format(
                "%s:%s:%s",
                runtimeIdentity.getWorkspaceId(),
                runtimeIdentity.getEnvName(),
                runtimeIdentity.getOwner())
            + " -push-endpoint "
            + installerWebsocketEndpoint
            + " -push-logs-endpoint "
            + outputWebsocketEndpoint
            + " -enable-auth"
            + " -server-check-period "
            + serverCheckPeriodSeconds
            + " -installer-timeout "
            + installerTimeoutSeconds
            + " -file "
            + BOOTSTRAPPER_DIR
            + CONFIG_FILE,
        null);
  }

  private void injectBootstrapper() throws InfrastructureException {
    dockerMachine.putResource(
        BOOTSTRAPPER_BASE_DIR,
        Thread.currentThread().getContextClassLoader().getResourceAsStream("bootstrapper.tar.gz"));
    // inject config file
    File configFileArchive = null;
    try {
      configFileArchive = createArchive(CONFIG_FILE, GSON.toJson(installers));
      dockerMachine.putResource(BOOTSTRAPPER_DIR, new FileInputStream(configFileArchive));
    } catch (FileNotFoundException e) {
      throw new InternalInfrastructureException(e.getMessage(), e);
    } finally {
      if (configFileArchive != null) {
        FileCleaner.addFile(configFileArchive);
      }
    }
  }

  private File createArchive(String filename, String content) throws InfrastructureException {
    Path bootstrapperConfTmp = null;
    try {
      bootstrapperConfTmp = Files.createTempDirectory(filename);
      Path configFile = bootstrapperConfTmp.resolve(filename);
      Files.copy(new ByteArrayInputStream(content.getBytes()), configFile);
      Path bootstrapperConfArchive = Files.createTempFile(filename, ".tar.gz");
      TarUtils.tarFiles(bootstrapperConfArchive.toFile(), configFile.toFile());
      return bootstrapperConfArchive.toFile();
    } catch (IOException e) {
      throw new InternalInfrastructureException(
          "Error occurred while injecting bootstrapping conf. " + e.getMessage(), e);
    } finally {
      if (bootstrapperConfTmp != null) {
        FileCleaner.addFile(bootstrapperConfTmp.toFile());
      }
    }
  }
}
