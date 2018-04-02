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
package org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.bootstrap.AbstractBootstrapper;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstraps installers in Kubernetes machine.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesBootstrapper extends AbstractBootstrapper {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesBootstrapper.class);

  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

  private static final String BOOTSTRAPPER_BASE_DIR = "/tmp/";
  private static final String BOOTSTRAPPER_DIR = BOOTSTRAPPER_BASE_DIR + "bootstrapper/";
  private static final String BOOTSTRAPPER_FILE = "bootstrapper";
  private static final String CONFIG_FILE = "config.json";

  private final RuntimeIdentity runtimeIdentity;
  private final List<? extends Installer> installers;
  private final int serverCheckPeriodSeconds;
  private final int installerTimeoutSeconds;
  private final KubernetesMachine kubernetesMachine;
  private final String bootstrapperBinaryUrl;
  private final String bootstrapperLogsFolder;
  private final String bootstrapperLogsFile;

  @Inject
  public KubernetesBootstrapper(
      @Assisted RuntimeIdentity runtimeIdentity,
      @Assisted List<? extends Installer> installers,
      @Assisted KubernetesMachine kubernetesMachine,
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      @Named("che.infra.kubernetes.bootstrapper.binary_url") String bootstrapperBinaryUrl,
      @Named("che.infra.kubernetes.bootstrapper.installer_timeout_sec") int installerTimeoutSeconds,
      @Named("che.infra.kubernetes.bootstrapper.server_check_period_sec")
          int serverCheckPeriodSeconds,
      @Named("che.workspace.logs.root_dir") String logsRootPath,
      EventService eventService) {
    super(
        kubernetesMachine.getName(),
        runtimeIdentity,
        cheWebsocketEndpoint,
        cheWebsocketEndpoint,
        eventService);
    this.bootstrapperBinaryUrl = bootstrapperBinaryUrl;
    this.runtimeIdentity = runtimeIdentity;
    this.installers = installers;
    this.serverCheckPeriodSeconds = serverCheckPeriodSeconds;
    this.installerTimeoutSeconds = installerTimeoutSeconds;
    this.kubernetesMachine = kubernetesMachine;
    this.bootstrapperLogsFolder = logsRootPath + "/bootstrapper";
    this.bootstrapperLogsFile = bootstrapperLogsFolder + "/bootstrapper.log";
  }

  @Override
  protected void doBootstrapAsync(String installerWebsocketEndpoint, String outputWebsocketEndpoint)
      throws InfrastructureException {
    injectBootstrapper();

    kubernetesMachine.exec(
        "sh",
        "-c",
        BOOTSTRAPPER_DIR
            + BOOTSTRAPPER_FILE
            + " -machine-name "
            + kubernetesMachine.getName()
            + " -runtime-id "
            + String.format(
                "%s:%s:%s",
                runtimeIdentity.getWorkspaceId(),
                runtimeIdentity.getEnvName(),
                runtimeIdentity.getOwnerId())
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
            + bootstrapperLogsFile
            + " 2>&1 &");
  }

  private void injectBootstrapper() throws InfrastructureException {
    String machineName = kubernetesMachine.getName();

    LOG.debug(
        "Bootstrapping {}:{}. Creating folder for bootstrapper", runtimeIdentity, machineName);
    kubernetesMachine.exec("mkdir", "-p", BOOTSTRAPPER_DIR, bootstrapperLogsFolder);
    LOG.debug("Bootstrapping {}:{}. Downloading bootstrapper binary", runtimeIdentity, machineName);
    kubernetesMachine.exec(
        "curl", "-o", BOOTSTRAPPER_DIR + BOOTSTRAPPER_FILE, bootstrapperBinaryUrl);
    kubernetesMachine.exec("chmod", "+x", BOOTSTRAPPER_DIR + BOOTSTRAPPER_FILE);

    LOG.debug("Bootstrapping {}:{}. Creating config file", runtimeIdentity, machineName);

    kubernetesMachine.exec("sh", "-c", "rm " + BOOTSTRAPPER_DIR + CONFIG_FILE);

    List<String> contentsToContatenate = new ArrayList<String>();
    contentsToContatenate.add("[");
    boolean firstOne = true;
    for (Installer installer : installers) {
      if (firstOne) {
        firstOne = false;
      } else {
        contentsToContatenate.add(",");
      }
      contentsToContatenate.add(GSON.toJson(installer));
    }
    contentsToContatenate.add("]");
    for (String content : contentsToContatenate) {
      kubernetesMachine.exec(
          "sh",
          "-c",
          "cat >> " + BOOTSTRAPPER_DIR + CONFIG_FILE + " << 'EOF'\n" + content + "\nEOF");
    }
  }
}
