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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import javax.inject.Inject;
import javax.inject.Named;

public class GatewayHostExternalServiceExposureStrategy
    extends SingleHostExternalServiceExposureStrategy implements ExternalServiceExposureStrategy {

  public static final String GATEWAY_HOST_STRATEGY = "gateway-host";

  @Inject
  public GatewayHostExternalServiceExposureStrategy(@Named("che.host") String cheHost) {
    super(cheHost);
  }
}
