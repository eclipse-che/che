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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import com.google.common.base.Splitter;
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
 * Checks if config maps with CA bundles is configured by specific property. If they are, then
 * creates single config map for all ca bundles in workspace namespace and mounts it into pods. If
 * ca-bundle auto-inject label is set, it allows Openshift 4+ to auto-inject cluster ca-bundle into
 * it. (see
 * https://docs.openshift.com/container-platform/4.3/networking/configuring-a-custom-pki.html#certificate-injection-using-operators_configuring-a-custom-pki)
 */
@Singleton
public class OpenshiftTrustedCAProvisioner extends KubernetesTrustedCAProvisioner {

  @Inject
  public OpenshiftTrustedCAProvisioner(
      @Nullable @Named("che.trusted_ca_bundles_configmap") String caBundleConfigMap,
      @Named("che.infra.openshift.trusted_ca_bundles_config_map") String configMapName,
      @Named("che.infra.openshift.trusted_ca_bundles_config_map_labels") String configMapLabel,
      @Named("che.infra.openshift.trusted_ca_bundles_mount_path") String certificateMountPath,
      CheInstallationLocation cheInstallationLocation,
      OpenShiftProjectFactory projectFactory,
      CheServerKubernetesClientFactory cheServerClientFactory)
      throws InfrastructureException {
    super(
        caBundleConfigMap,
        configMapName,
        certificateMountPath,
        cheInstallationLocation,
        projectFactory,
        cheServerClientFactory);
    this.configMapLabelKeyValue = Splitter.on(",").withKeyValueSeparator("=").split(configMapLabel);
  }
}
