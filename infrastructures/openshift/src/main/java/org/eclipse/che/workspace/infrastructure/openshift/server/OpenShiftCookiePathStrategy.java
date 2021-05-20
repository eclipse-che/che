/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;

import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.CookiePathStrategy;

/**
 * On OpenShift we always use the equivalent of the multi-host strategy and therefore use the
 * appropriate cookie path strategy for the secured endpoints.
 */
public class OpenShiftCookiePathStrategy extends CookiePathStrategy {

  public OpenShiftCookiePathStrategy() {
    super(MULTI_HOST_STRATEGY);
  }
}
