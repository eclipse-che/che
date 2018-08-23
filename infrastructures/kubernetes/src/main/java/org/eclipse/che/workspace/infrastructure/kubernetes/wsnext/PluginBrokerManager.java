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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsnext;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsnext.model.PluginMeta;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsnext.brokerphases.DeliverMetas;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsnext.brokerphases.DeployBroker;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsnext.brokerphases.ListenBrokerEvents;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsnext.brokerphases.PrepareStorage;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsnext.brokerphases.WaitBrokerResult;

/**
 * Deploys Che plugin broker in a workspace, receives result of its execution and return resolved
 * workspace tooling or error of plugin broker execution.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class PluginBrokerManager {

  private static final String PVC_CLAIM_PROJECTS = "claim-che-workspace";
  private static final String CONFIG_MAP_NAME_SUFFIX = "broker-config-map";
  private static final String BROKER_VOLUME = "broker-config-volume";
  private static final String CONF_FOLDER = "/broker-config";
  private static final String CONFIG_FILE = "config.json";

  private final KubernetesNamespaceFactory factory;
  private final EventService eventService;
  private final WorkspaceVolumesStrategy volumesStrategy;
  private final String pvcName;
  private final String pvcQuantity;
  private final String pvcAccessMode;
  private final String cheWebsocketEndpoint;
  private final String pluginBrokerImage;

  @Inject
  public PluginBrokerManager(
      KubernetesNamespaceFactory factory,
      EventService eventService,
      WorkspaceVolumesStrategy volumesStrategy,
      @Named("che.infra.kubernetes.pvc.name") String pvcName,
      @Named("che.infra.kubernetes.pvc.quantity") String pvcQuantity,
      @Named("che.infra.kubernetes.pvc.access_mode") String pvcAccessMode,
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      @Named("che.workspace.plugin_broker.image") String pluginBrokerImage) {
    this.factory = factory;
    this.eventService = eventService;
    this.volumesStrategy = volumesStrategy;
    this.pvcName = pvcName;
    this.pvcQuantity = pvcQuantity;
    this.pvcAccessMode = pvcAccessMode;
    this.cheWebsocketEndpoint = cheWebsocketEndpoint;
    this.pluginBrokerImage = pluginBrokerImage;
  }

  /**
   * Deploys Che plugin broker in a workspace, receives result of its execution and return resolved
   * workspace tooling or error of plugin broker execution.
   *
   * <p>This API is in <b>Beta</b> and is subject to changes or removal.
   */
  @Beta
  public List<ChePlugin> getTooling(
      RuntimeIdentity runtimeID,
      Collection<PluginMeta> pluginsMeta,
      KubernetesEnvironment environment)
      throws InfrastructureException {

    String workspaceId = runtimeID.getWorkspaceId();
    CompletableFuture<List<ChePlugin>> toolingFuture = new CompletableFuture<>();
    KubernetesNamespace kubernetesNamespace = factory.create(workspaceId);

    String configMapName = generateUniqueConfigMapName();

    WaitBrokerResult waitBrokerResult = new WaitBrokerResult(toolingFuture);
    DeployBroker deployBroker =
        new DeployBroker(
            kubernetesNamespace,
            workspaceId,
            cheWebsocketEndpoint,
            CONF_FOLDER,
            CONFIG_FILE,
            PVC_CLAIM_PROJECTS,
            BROKER_VOLUME,
            configMapName,
            pluginBrokerImage,
            waitBrokerResult);
    DeliverMetas deliverMetas =
        new DeliverMetas(
            deployBroker, kubernetesNamespace, pluginsMeta, CONFIG_FILE, configMapName);
    PrepareStorage prepareStorage =
        new PrepareStorage(deliverMetas,
            workspaceId,
            environment,
            volumesStrategy,
            pvcName,
            pvcAccessMode,
            pvcQuantity);
    ListenBrokerEvents listenBrokerEvents =
        new ListenBrokerEvents(prepareStorage, workspaceId, toolingFuture, eventService);

    return listenBrokerEvents.execute();
  }

  private String generateUniqueConfigMapName() {
    return NameGenerator.generate(CONFIG_MAP_NAME_SUFFIX, 6);
  }
}
