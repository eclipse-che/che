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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.DefaultHostExternalServiceExposureStrategy.DEFAULT_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressServiceExposureStrategyProvider.STRATEGY_PROPERTY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostExternalServiceExposureStrategy.SINGLE_HOST_STRATEGY;

import com.google.inject.Inject;
import java.util.function.Function;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;

/**
 * This is a helper class to {@link JwtProxyProvisioner} that helps it figure out the prefix on the
 * "pathBase" on which it should be exposed. This is used both as a prefix on the pathBase and as
 * the path set on the authentication cookie.
 *
 * <p>The prefix is based on the workspace ID so that we can have separate authentication cookies
 * per workspace.
 */
@Singleton
public class PathBasePrefixProvider {

  private final Function<RuntimeIdentity, String> extractor;

  @Inject
  public PathBasePrefixProvider(@Named(STRATEGY_PROPERTY) String serverStrategy) {
    switch (serverStrategy) {
      case DEFAULT_HOST_STRATEGY:
      case SINGLE_HOST_STRATEGY:
        extractor = RuntimeIdentity::getWorkspaceId;
        break;
      case MULTI_HOST_STRATEGY:
        extractor = __ -> "";
        break;
      default:
        throw new IllegalArgumentException(
            format(
                "Unsupported value '%s' for the configuration property %s",
                serverStrategy, STRATEGY_PROPERTY));
    }
  }

  public String getPathPrefix(RuntimeIdentity identity) {
    return extractor.apply(identity);
  }
}
