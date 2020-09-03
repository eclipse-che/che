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
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.isLabeled;

import com.google.common.base.Splitter;
import io.fabric8.kubernetes.api.model.ConfigMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/** Little utility bean helping with Gateway ConfigMaps labels. */
@Singleton
public class GatewayConfigmapLabels {

  private final Map<String, String> labels;

  @Inject
  public GatewayConfigmapLabels(
      @Named("che.infra.kubernetes.single_host.gateway.configmap.labels") String labelsProperty)
      throws InfrastructureException {
    if (labelsProperty == null || labelsProperty.isEmpty()) {
      throw new InfrastructureException(
          "for gateway single-host, 'che.infra.kubernetes.single_host.gateway.configmap.labels' property must be defined");
    }
    try {
      this.labels = Splitter.on(",").trimResults().withKeyValueSeparator("=").split(labelsProperty);
    } catch (IllegalArgumentException iae) {
      throw new InfrastructureException(
          "'che.infra.kubernetes.single_host.gateway.configmap.labels' is set to invalid value. It must be in format `name1=value1,name2=value2`. Check the documentation for further details.",
          iae);
    }
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  /**
   * Check whether configmap is gateway route configuration. That is defined with labels provided by
   * `che.infra.kubernetes.single_host.gateway.configmap.labels` configuration property.
   *
   * @param configMap to check
   * @return `true` if ConfigMap is gateway route configuration, `false` otherwise
   */
  public boolean isGatewayConfig(ConfigMap configMap) {
    for (Entry<String, String> labelEntry : labels.entrySet()) {
      if (!isLabeled(configMap, labelEntry.getKey(), labelEntry.getValue())) {
        return false;
      }
    }
    return true;
  }
}
