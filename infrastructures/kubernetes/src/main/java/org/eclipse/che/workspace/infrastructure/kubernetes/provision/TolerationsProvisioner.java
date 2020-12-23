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
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provisions tolerations into workspace pod spec. */
public class TolerationsProvisioner implements ConfigurationProvisioner {

  private final List<Toleration> tolerations;
  private static final Logger LOG = LoggerFactory.getLogger(TolerationsProvisioner.class);

  @Inject
  public TolerationsProvisioner(
      @Nullable @Named("che.workspace.pod.tolerations") String tolerationsProperty)
      throws JsonProcessingException {
    LOG.info("CHKPNT: TolerationsProvisioner created with {}", tolerationsProperty);
    ObjectMapper jsonMapper = new ObjectMapper();
    this.tolerations =
        tolerationsProperty != null
            ? jsonMapper.readValue(tolerationsProperty, new TypeReference<List<Toleration>>() {})
            : emptyList();
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (!tolerations.isEmpty()) {
      k8sEnv
          .getPodsData()
          .values()
          .forEach(
              d -> {
                LOG.info(
                    "CHKPNT: adding tolerations '[{}] {} {} {}' to pod {}/{}",
                    tolerations.get(0).getEffect(),
                    tolerations.get(0).getKey(),
                    tolerations.get(0).getOperator(),
                    tolerations.get(0).getValue(),
                    d.getMetadata().getNamespace(),
                    d.getMetadata().getName());
                d.getSpec().setTolerations(tolerations);
              });
    } else {
      LOG.info("CHKPNT: No tolerations available");
    }
  }
}
