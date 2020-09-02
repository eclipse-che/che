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

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.isLabeled;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/** Little utility bean helping with Gateway ConfigMaps labels. */
@Singleton
public class GatewayConfigmapLabels {

  private final Map<String, String> labels;

  @Inject
  public GatewayConfigmapLabels(
      @Named("che.infra.kubernetes.single_host.gateway.configmap.labels") String[] labelsProperty) {
    // TODO: use Collectors.toUnmodifiableMap when JDK11 features are allowed
    this.labels =
        ImmutableMap.copyOf(
            Arrays.stream(labelsProperty)
                .map(label -> label.split("=", 2))
                .collect(toMap(l -> l[0], l -> l[1])));
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
