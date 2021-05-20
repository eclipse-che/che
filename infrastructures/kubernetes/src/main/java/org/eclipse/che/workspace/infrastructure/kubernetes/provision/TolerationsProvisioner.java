/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.Toleration;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/** Provisions tolerations into workspace pod spec. */
public class TolerationsProvisioner implements ConfigurationProvisioner {

  private final List<Toleration> tolerations;

  @Inject
  public TolerationsProvisioner(
      @Nullable @Named("che.workspace.pod.tolerations_json") String tolerationsProperty)
      throws ConfigurationException {
    try {
      ObjectMapper jsonMapper = new ObjectMapper();
      this.tolerations =
          tolerationsProperty != null
              ? jsonMapper.readValue(tolerationsProperty, new TypeReference<List<Toleration>>() {})
              : emptyList();
    } catch (JsonProcessingException e) {
      throw new ConfigurationException(
          "che.workspace.pod.tolerations_json contains an invalid JSON string", e);
    }
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (!tolerations.isEmpty()) {
      k8sEnv.getPodsData().values().forEach(d -> d.getSpec().setTolerations(tolerations));
    }
  }
}
