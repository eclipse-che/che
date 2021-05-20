/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.util.ApiInfoLogInformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Prints warning into logs on startup when Che is running in non-TLS mode. */
@Singleton
public class NonTlsDistributedClusterModeNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(ApiInfoLogInformer.class);

  private final boolean isTlsEnabled;

  @Inject
  public NonTlsDistributedClusterModeNotifier(
      @Named("che.infra.kubernetes.tls_enabled") boolean isTlsEnabled) {
    this.isTlsEnabled = isTlsEnabled;
  }

  @PostConstruct
  public void printWarnOnStartup() {
    if (!isTlsEnabled) {
      LOG.warn(
          "Eclipse Che deployed on non-TLS mode. This may cause client-side problems opening workspaces on multi-cluster installations."
              + " See https://eclipse.org/che/docs/che-7/introduction-to-eclipse-che/#problems-opening-workspace-in-newest-chrome-versions-on-non-tls-installations-on-distributed-clusters for details.");
    }
  }
}
