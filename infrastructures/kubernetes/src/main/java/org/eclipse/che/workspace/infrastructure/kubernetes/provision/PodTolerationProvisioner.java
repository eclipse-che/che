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

import com.google.common.base.Strings;
import io.fabric8.kubernetes.api.model.Toleration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Overrides trelations.
 *
 * <p>Note: if `terminationGracePeriodSeconds` have been explicitly set in Kubernetes / OpenShift
 * recipe it will not be overridden
 *
 * @author Masaki Muranaka (monaka@monami-ya.com)
 */
public class PodTolerationProvisioner implements ConfigurationProvisioner {
  private final String tolerationSettings;

  class InLambdaException extends RuntimeException {
    InLambdaException(String msg) {
      super(msg);
    }
  }

  @Inject
  public PodTolerationProvisioner(
      @Named("che.infra.kubernetes.pod.tolerations") String tolerationSettings) {
    this.tolerationSettings = tolerationSettings;
  }

  private Toleration buildToleration(String chunk) {
    if (Strings.isNullOrEmpty(chunk)) {
      throw new InLambdaException("Can't parse che.infra.kubernetes.pod.tolerations");
    }

    final String[] chunks = chunk.split(":");
    final Toleration toleration = new Toleration();
    toleration.setEffect(chunks[0]);
    if (chunks.length >= 2 && !Strings.isNullOrEmpty(chunks[1])) {
      toleration.setKey(chunks[1]);
      if (chunks.length >= 3 && !Strings.isNullOrEmpty(chunks[2])) {
        toleration.setOperator(chunks[2]);
        if (chunks.length >= 4 && !Strings.isNullOrEmpty(chunks[3])) {
          toleration.setTolerationSeconds(Long.parseLong(chunks[3]));
          if (chunks.length == 5 && !Strings.isNullOrEmpty(chunks[4])) {
            toleration.setValue(chunks[4]);
          } else {
            /* chunk.length >= 5 */
            throw new InLambdaException("Can't parse che.infra.kubernetes.pod.tolerations");
          }
        }
      }
    }
    return toleration;
  }

  private List<Toleration> parseSettings(String settings) throws InfrastructureException {
    final String[] chunk = settings.split(",");
    try {
      return Arrays.stream(chunk).map(x -> buildToleration(x)).collect(Collectors.toList());
    } catch (InLambdaException | NumberFormatException e) {
      throw new InfrastructureException(e);
    }
  }

  @Override
  @Traced
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    TracingTags.WORKSPACE_ID.set(identity::getWorkspaceId);
    if (Strings.isNullOrEmpty(tolerationSettings)) {
      return;
    }

    final List<Toleration> tolerations = parseSettings(tolerationSettings);
    for (PodData pod : k8sEnv.getPodsData().values()) {
      pod.getSpec().setTolerations(tolerations);
    }
  }
}
