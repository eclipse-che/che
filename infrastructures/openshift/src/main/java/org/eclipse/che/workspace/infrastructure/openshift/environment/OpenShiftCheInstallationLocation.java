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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenShiftCheInstallationLocation checks the KUBERNETES_NAMESPACE and POD_NAMESPACE environment variables
 * to determine what namespace Che is installed in.  Users should use this class to retrieve the installation
 * namespace name.
 *
 * @author Tom George
 */
@Singleton
public class OpenShiftCheInstallationLocation {

  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftCheInstallationLocation.class);

  private String installationLocationNamespace;

  /** @return The name of the namespace where Che is installed */
  public String getInstallationLocationNamespace() {
    return installationLocationNamespace;
  }

  @PostConstruct
  public void postConstruct() {
    String kubernetesNamespace = System.getenv("KUBERNETES_NAMESPACE");
    String podNamespace = System.getenv("POD_NAMESPACE");

    if (kubernetesNamespace == null && podNamespace == null) {
      LOG.info(
          "Neither KUBERNETES_NAMESPACE nor POD_NAMESPACE is defined.  Unable to determine Che installation location");
    }
    installationLocationNamespace =
        kubernetesNamespace == null ? podNamespace : kubernetesNamespace;
  }
}
