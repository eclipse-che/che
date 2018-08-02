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
package org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.assistedinject.Assisted;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.bootstrap.AbstractBootstrapper;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.MachineLogEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizer;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstraps installers in Kubernetes machine.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesBootstrapper extends AbstractBootstrapper {
  private static final int EXEC_TIMEOUT_MIN = 5;

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
  private final KubernetesMachineImpl kubernetesMachine;
  private final String bootstrapperBinaryUrl;
  private final String bootstrapperLogsFolder;
  private final String bootstrapperLogsFile;
  private final EventService eventService;
  private final KubernetesNamespace namespace;
  private final StartSynchronizer startSynchronizer;

  @Inject
  public KubernetesBootstrapper(
      @Assisted RuntimeIdentity runtimeIdentity,
      @Assisted List<? extends Installer> installers,
      @Assisted KubernetesMachineImpl kubernetesMachine,
      @Assisted KubernetesNamespace namespace,
      @Assisted StartSynchronizer startSynchronizer,
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
    this.eventService = eventService;
    this.namespace = namespace;
    this.bootstrapperLogsFile = bootstrapperLogsFolder + "/bootstrapper.log";
    this.startSynchronizer = startSynchronizer;
  }

  @Override
  protected void doBootstrapAsync(String installerWebsocketEndpoint, String outputWebsocketEndpoint)
      throws InfrastructureException {
    injectBootstrapper();

    startSynchronizer.checkFailure();
    exec(
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
    final String mName = kubernetesMachine.getName();
    final RuntimeIdentityDto runtimeIdentityDto = DtoConverter.asDto(runtimeIdentity);
    final BiConsumer<String, String> outputConsumer =
        (stream, text) ->
            eventService.publish(
                DtoFactory.newDto(MachineLogEvent.class)
                    .withRuntimeId(runtimeIdentityDto)
                    .withStream(stream)
                    .withText(text)
                    .withTime(ZonedDateTime.now().format(ISO_OFFSET_DATE_TIME))
                    .withMachineName(mName));
    startSynchronizer.checkFailure();
    LOG.debug("Bootstrapping {}:{}. Creating folder for bootstrapper", runtimeIdentity, mName);
    exec(outputConsumer, "mkdir", "-p", BOOTSTRAPPER_DIR, bootstrapperLogsFolder);

    startSynchronizer.checkFailure();
    LOG.debug("Bootstrapping {}:{}. Downloading bootstrapper binary", runtimeIdentity, mName);
    exec(
        outputConsumer,
        "curl",
        "-fsSo",
        BOOTSTRAPPER_DIR + BOOTSTRAPPER_FILE,
        bootstrapperBinaryUrl);
    exec(outputConsumer, "chmod", "+x", BOOTSTRAPPER_DIR + BOOTSTRAPPER_FILE);

    startSynchronizer.checkFailure();
    LOG.debug("Bootstrapping {}:{}. Creating config file", runtimeIdentity, mName);
    exec("sh", "-c", "rm " + BOOTSTRAPPER_DIR + CONFIG_FILE);

    List<String> contentsToConcatenate = new ArrayList<>();
    contentsToConcatenate.add("[");
    boolean firstOne = true;
    for (Installer installer : installers) {
      if (firstOne) {
        firstOne = false;
      } else {
        contentsToConcatenate.add(",");
      }
      contentsToConcatenate.add(GSON.toJson(installer));
    }
    contentsToConcatenate.add("]");
    for (String content : contentsToConcatenate) {
      startSynchronizer.checkFailure();
      exec(
          "sh",
          "-c",
          "cat >> " + BOOTSTRAPPER_DIR + CONFIG_FILE + " << 'EOF'\n" + content + "\nEOF");
    }
  }

  private void exec(BiConsumer<String, String> outputConsumer, String... command)
      throws InfrastructureException {
    namespace
        .deployments()
        .exec(
            kubernetesMachine.getPodName(),
            kubernetesMachine.getContainerName(),
            EXEC_TIMEOUT_MIN,
            command,
            outputConsumer);
  }

  private void exec(String... command) throws InfrastructureException {
    namespace
        .deployments()
        .exec(
            kubernetesMachine.getPodName(),
            kubernetesMachine.getContainerName(),
            EXEC_TIMEOUT_MIN,
            command);
  }
}
