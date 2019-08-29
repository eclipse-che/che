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
package org.eclipse.che.workspace.infrastructure.openshift.server;

import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.PathBasePrefixProvider;

/**
 * Regardless of any configured server strategy, on OpenShift we always use the multi-host server
 * strategy.
 */
public class OpenShiftPathBasePrefixProvider extends PathBasePrefixProvider {
  public OpenShiftPathBasePrefixProvider() {
    super(MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY);
  }
}
