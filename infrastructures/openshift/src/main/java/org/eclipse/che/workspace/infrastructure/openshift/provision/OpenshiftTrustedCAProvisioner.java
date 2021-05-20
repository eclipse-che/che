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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.CheInstallationLocation;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesTrustedCAProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;

/**
 * This class overrides CA bundle config map labels (from
 * che.infra.openshift.trusted_ca.dest_configmap_labels) to be able to include OpenShift 4+ specific
 * label config.openshift.io/inject-trusted-cabundle=true that makes OpenShift inject cluster CA
 * bundle into resulting config map. For more details see
 * https://docs.openshift.com/container-platform/4.3/networking/configuring-a-custom-pki.html#certificate-injection-using-operators_configuring-a-custom-pki
 */
@Singleton
public class OpenshiftTrustedCAProvisioner extends KubernetesTrustedCAProvisioner {

  @Inject
  public OpenshiftTrustedCAProvisioner(
      @Nullable @Named("che.infra.kubernetes.trusted_ca.src_configmap") String caBundleConfigMap,
      @Named("che.infra.kubernetes.trusted_ca.dest_configmap") String configMapName,
      @Named("che.infra.kubernetes.trusted_ca.mount_path") String certificateMountPath,
      @Nullable @Named("che.infra.openshift.trusted_ca.dest_configmap_labels")
          String configMapLabel,
      CheInstallationLocation cheInstallationLocation,
      OpenShiftProjectFactory projectFactory,
      CheServerKubernetesClientFactory cheServerClientFactory)
      throws InfrastructureException {
    super(
        caBundleConfigMap,
        configMapName,
        certificateMountPath,
        configMapLabel,
        cheInstallationLocation,
        projectFactory,
        cheServerClientFactory);
  }
}
