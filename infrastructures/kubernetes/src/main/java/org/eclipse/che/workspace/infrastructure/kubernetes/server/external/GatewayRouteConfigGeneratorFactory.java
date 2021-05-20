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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import javax.inject.Singleton;

/**
 * This Factory provides {@link GatewayRouteConfigGenerator} instances, so implementation using
 * these can stay Gateway technology agnostic.
 */
@Singleton
public class GatewayRouteConfigGeneratorFactory {

  public GatewayRouteConfigGenerator create() {
    return new TraefikGatewayRouteConfigGenerator();
  }
}
