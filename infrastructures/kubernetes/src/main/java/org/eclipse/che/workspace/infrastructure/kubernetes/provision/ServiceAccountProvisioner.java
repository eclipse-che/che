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

import static com.google.common.base.Strings.isNullOrEmpty;

import io.fabric8.kubernetes.api.model.Pod;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Sets the service account to workspace pods if configured.
 *
 * <p>Service account won't be set to pods if property value is `NULL` and then Kubernetes
 * infrastructure will set default one.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class ServiceAccountProvisioner implements ConfigurationProvisioner {

  private final String serviceAccount;

  @Inject
  public ServiceAccountProvisioner(
      @Nullable @Named("che.infra.kubernetes.service_account_name") String serviceAccount) {
    this.serviceAccount = serviceAccount;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    if (!isNullOrEmpty(serviceAccount)) {
      for (Pod pod : k8sEnv.getPods().values()) {
        pod.getSpec().setServiceAccountName(serviceAccount);
        pod.getSpec().setAutomountServiceAccountToken(true);
      }
    }
  }
}
