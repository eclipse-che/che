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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.annotations.Beta;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.slf4j.Logger;

/**
 * Creates config map that can inject Che tooling plugins meta files into a Che plugin broker in a
 * workspace. Then calls next {@link BrokerPhase} and removes config map after next phase completes.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class DeliverMetas extends BrokerPhase {

  private static final Logger LOG = getLogger(DeliverMetas.class);
  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

  private final KubernetesNamespace kubernetesNamespace;
  private final Collection<PluginMeta> pluginsMeta;
  private final String configFile;
  private final String configMapName;

  public DeliverMetas(
      KubernetesNamespace kubernetesNamespace,
      Collection<PluginMeta> pluginsMeta,
      String configFile,
      String configMapName) {
    this.kubernetesNamespace = kubernetesNamespace;
    this.pluginsMeta = pluginsMeta;
    this.configFile = configFile;
    this.configMapName = configMapName;
  }

  @Override
  public List<ChePlugin> execute() throws InfrastructureException {
    try {
      kubernetesNamespace.configMaps().create(newConfigMap(pluginsMeta));

      return nextPhase.execute();
    } finally {
      try {
        kubernetesNamespace.configMaps().delete();
      } catch (InfrastructureException ex) {
        LOG.error(
            "Broker deployment config map removal failed. Error: " + ex.getLocalizedMessage(), ex);
      }
    }
  }

  private ConfigMap newConfigMap(Collection<PluginMeta> pluginsMetas) {
    Map<String, String> initConfigMapData = new HashMap<>();
    initConfigMapData.put(configFile, stringYaml(pluginsMetas));

    return new ConfigMapBuilder()
        .withNewMetadata()
        .withName(configMapName)
        .endMetadata()
        .withData(initConfigMapData)
        .build();
  }

  private String stringYaml(Collection<PluginMeta> pluginsMetas) {
    return GSON.toJson(pluginsMetas);
  }
}
