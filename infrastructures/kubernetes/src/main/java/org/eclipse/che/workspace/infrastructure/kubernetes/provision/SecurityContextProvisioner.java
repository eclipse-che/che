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

import io.fabric8.kubernetes.api.model.PodSecurityContextBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/** @author Sergii Leshchenko */
public class SecurityContextProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  private final Long runAsUser;
  private final Long fsGroup;

  @Inject
  public SecurityContextProvisioner(
      @Nullable @Named("che.infra.kubernetes.pod.security_context.run_as_user") String runAsUser,
      @Nullable @Named("che.infra.kubernetes.pod.security_context.fs_group") String fsGroup) {
    this.runAsUser = runAsUser == null ? null : Long.parseLong(runAsUser);
    this.fsGroup = fsGroup == null ? null : Long.parseLong(fsGroup);
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (runAsUser != null) {
      k8sEnv.getPodData().values().forEach(p -> provision(p.getSpec()));
    }
  }

  public void provision(PodSpec podSpec) {
    podSpec.setSecurityContext(
        new PodSecurityContextBuilder().withRunAsUser(runAsUser).withFsGroup(fsGroup).build());
  }
}
