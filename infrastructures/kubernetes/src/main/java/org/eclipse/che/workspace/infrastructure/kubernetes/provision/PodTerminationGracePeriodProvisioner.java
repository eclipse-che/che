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

import static java.lang.Boolean.parseBoolean;
import static org.eclipse.che.api.workspace.shared.Constants.ASYNC_PERSIST_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.EphemeralWorkspaceUtility.isEphemeral;

import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.Map;
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
  /**
   * This value will activate if workspace configured to use Async Storage. We can't set default
   * grace termination period because we need to give some time on workspace stop action for backup
   * changes to the persistent storage. At the moment no way to predict this time because it depends
   * on amount of files, size of files and network ability. This is some empirical number of seconds
   * which should be enough for most projects.
   */
  private static final long GRACE_TERMINATION_PERIOD_ASYNC_STORAGE_WS_SEC = 60;

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

    for (PodData pod : k8sEnv.getPodsData().values()) {
      if (!isTerminationGracePeriodSet(pod.getSpec())) {
        pod.getSpec().setTerminationGracePeriodSeconds(getGraceTerminationPeriodSec(k8sEnv));
      }
    }
  }

  /**
   * Returns true if 'terminationGracePeriodSeconds' have been explicitly set in Kubernetes /
   * OpenShift recipe, false otherwise
   */
  private boolean isTerminationGracePeriodSet(final PodSpec podSpec) {
    return podSpec.getTerminationGracePeriodSeconds() != null;
  }

  private long getGraceTerminationPeriodSec(KubernetesEnvironment k8sEnv) {
    Map<String, String> attributes = k8sEnv.getAttributes();
    if (isEphemeral(attributes) && parseBoolean(attributes.get(ASYNC_PERSIST_ATTRIBUTE))) {
      return GRACE_TERMINATION_PERIOD_ASYNC_STORAGE_WS_SEC;
    }
    return graceTerminationPeriodSec;
  }
}
