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
package org.eclipse.che.workspace.infrastructure.openshift.bootstrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.bootstrap.AbstractBootstrapper;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstraps installers in OpenShift machine.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftBootstrapper extends AbstractBootstrapper {
  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftBootstrapper.class);

  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

  private static final String BOOTSTRAPPER_BASE_DIR = "/tmp/";
  private static final String BOOTSTRAPPER_DIR = BOOTSTRAPPER_BASE_DIR + "bootstrapper/";
  private static final String BOOTSTRAPPER_FILE = "bootstrapper";
  private static final String BOOTSTRAPPER_LOG_FILE = "bootstrapper.log";
  private static final String CONFIG_FILE = "config.json";

  private final RuntimeIdentity runtimeIdentity;
  private final List<Installer> installers;
  private final int serverCheckPeriodSeconds;
  private final int installerTimeoutSeconds;
  private final OpenShiftMachine openShiftMachine;
  private final String bootstrapperBinaryUrl;

  @Inject
  public OpenShiftBootstrapper(
      @Assisted RuntimeIdentity runtimeIdentity,
      @Assisted List<Installer> installers,
      @Assisted OpenShiftMachine openShiftMachine,
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      @Named("che.infra.openshift.bootstrapper.binary_url") String bootstrapperBinaryUrl,
      @Named("che.infra.openshift.bootstrapper.timeout_min") int bootstrappingTimeoutMinutes,
      @Named("che.infra.openshift.bootstrapper.installer_timeout_sec") int installerTimeoutSeconds,
      @Named("che.infra.openshift.bootstrapper.server_check_period_sec")
          int serverCheckPeriodSeconds,
      EventService eventService) {
    super(
        openShiftMachine.getName(),
        runtimeIdentity,
        bootstrappingTimeoutMinutes,
        cheWebsocketEndpoint,
        cheWebsocketEndpoint,
        eventService);
    this.bootstrapperBinaryUrl = bootstrapperBinaryUrl;
    this.runtimeIdentity = runtimeIdentity;
    this.installers = installers;
    this.serverCheckPeriodSeconds = serverCheckPeriodSeconds;
    this.installerTimeoutSeconds = installerTimeoutSeconds;
    this.openShiftMachine = openShiftMachine;
  }

  @Override
  protected void doBootstrapAsync(String installerWebsocketEndpoint, String outputWebsocketEndpoint)
      throws InfrastructureException {
    injectBootstrapper();

    openShiftMachine.exec(
        "sh",
        "-c",
        BOOTSTRAPPER_DIR
            + BOOTSTRAPPER_FILE
            + " -machine-name "
            + openShiftMachine.getName()
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
            + " -server-check-period "
            + Integer.toString(serverCheckPeriodSeconds)
            + " -enable-auth"
            + " -installer-timeout "
            + Integer.toString(installerTimeoutSeconds)
            + " -file "
            + BOOTSTRAPPER_DIR
            + CONFIG_FILE
            // redirects command output and makes the bootstrapping process detached,
            // to avoid the holding of the socket connection for exec watcher.
            + " > "
            + BOOTSTRAPPER_DIR
            + BOOTSTRAPPER_LOG_FILE
            + " 2>&1 &");
  }

  private void injectBootstrapper() throws InfrastructureException {
    LOG.debug("Creating folder for bootstrapper");
    openShiftMachine.exec("mkdir", "-p", BOOTSTRAPPER_DIR);
    LOG.debug("Downloading bootstrapper binary");
    openShiftMachine.exec(
        "curl", "-o", BOOTSTRAPPER_DIR + BOOTSTRAPPER_FILE, bootstrapperBinaryUrl);
    openShiftMachine.exec("chmod", "+x", BOOTSTRAPPER_DIR + BOOTSTRAPPER_FILE);

    LOG.debug("Creating bootstrapper config file");
    openShiftMachine.exec(
        "sh",
        "-c",
        "cat > "
            + BOOTSTRAPPER_DIR
            + CONFIG_FILE
            + " << 'EOF'\n"
            + GSON.toJson(installers)
            + "\nEOF");
  }
}
