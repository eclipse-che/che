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

import io.fabric8.kubernetes.api.model.PodSpec;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Adds grace termination period to workspace pods.
 *
 * <p>Note: if `terminationGracePeriodSeconds` have been explicitly set in Kubernetes / OpenShift
 * recipe it will not be overridden
 *
 * @author Ilya Buziuk (ibuziuk@redhat.com)
 */
public class PodTerminationGracePeriodProvisioner implements ConfigurationProvisioner {
  private final long graceTerminationPeriodSec;

  @Inject
  public PodTerminationGracePeriodProvisioner(
      @Named("che.infra.kubernetes.pod.termination_grace_period_sec")
          long graceTerminationPeriodSec) {
    this.graceTerminationPeriodSec = graceTerminationPeriodSec;
  }

  @Override
  @Traced
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    TracingTags.WORKSPACE_ID.set(identity::getWorkspaceId);

    for (PodData pod : k8sEnv.getPodData().values()) {
      if (!isTerminationGracePeriodSet(pod.getSpec())) {
        pod.getSpec().setTerminationGracePeriodSeconds(graceTerminationPeriodSec);
      }
    }
  }

  /**
   * @param pod
   * @return true if 'terminationGracePeriodSeconds' have been explicitly set in Kubernetes /
   *     OpenShift recipe, false otherwise
   */
  private boolean isTerminationGracePeriodSet(final PodSpec podSpec) {
    return podSpec.getTerminationGracePeriodSeconds() != null;
  }
}
