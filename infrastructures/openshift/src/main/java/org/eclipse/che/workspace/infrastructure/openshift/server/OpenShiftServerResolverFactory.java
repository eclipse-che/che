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
package org.eclipse.che.workspace.infrastructure.openshift.server;

import static org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType.GATEWAY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType.NATIVE;

import com.google.common.collect.ImmutableMap;
import io.fabric8.openshift.api.model.Route;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.AbstractServerResolverFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.ConfigMapServerResolver;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.ServerResolver;

/**
 * Factory that decides by configuration, which {@link ServerResolver} implementation to use in
 * OpenShift environment.
 */
public class OpenShiftServerResolverFactory extends AbstractServerResolverFactory<Route> {

  @Inject
  public OpenShiftServerResolverFactory(
      @Named("che.host") String cheHost,
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy,
      @Named("che.infra.kubernetes.singlehost.workspace.exposure") String wsExposureType) {
    super(
        exposureStrategy,
        wsExposureType,
        ImmutableMap.of(
            GATEWAY,
                (ss, rs, cs) ->
                    new ConfigMapServerResolver(ss, cs, cheHost, new RouteServerResolver(ss, rs)),
            NATIVE, (ss, rs, cs) -> new RouteServerResolver(ss, rs)),
        "Failed to initialize OpenShiftServerResolverFactory for workspace exposure type '%s'.");
  }
}
