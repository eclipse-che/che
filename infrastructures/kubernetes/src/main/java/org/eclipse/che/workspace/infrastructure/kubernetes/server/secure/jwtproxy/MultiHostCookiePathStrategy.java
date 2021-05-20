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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;

import javax.inject.Singleton;

/**
 * A specialization of the {@link CookiePathStrategy} for multi-host server strategy. We need this
 * declared specifically to be able to use both the configured strategy and multi-host in case of
 * workspaces with mixed endpoints.
 */
@Singleton
public class MultiHostCookiePathStrategy extends CookiePathStrategy {
  public MultiHostCookiePathStrategy() {
    super(MULTI_HOST_STRATEGY);
  }
}
