/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.provision.route;

import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteSpec;
import io.fabric8.openshift.api.model.TLSConfig;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.Annotations;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.ConfigurationProvisioner;

/**
 * Enables Transport Layer Security (TLS) for workspace routes and changes protocol to secure (wss /
 * https) in servers' configuration
 *
 * @author Ilya Buziuk
 */
@Singleton
public class TlsRouteProvisioner implements ConfigurationProvisioner {
  private static final String TERMINATION_EDGE = "edge";
  private static final String TERMINATION_POLICY_REDIRECT = "Redirect";
  private final boolean isTlsEnabled;

  @Inject
  public TlsRouteProvisioner(@Named("che.infra.openshift.tls_enabled") boolean isTlsEnabled) {
    this.isTlsEnabled = isTlsEnabled;
  }

  @Override
  public void provision(OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (!isTlsEnabled) {
      return;
    }
    final Set<Route> routes = new HashSet<>(osEnv.getRoutes().values());
    for (Route route : routes) {
      useSecureProtocolForServers(route);
      enableTls(route);
    }
  }

  private void useSecureProtocolForServers(final Route route) {
    Map<String, ServerConfigImpl> servers =
        Annotations.newDeserializer(route.getMetadata().getAnnotations()).servers();

    servers.values().forEach(s -> s.setProtocol(getSecureProtocol(s.getProtocol())));

    Map<String, String> annotations = Annotations.newSerializer().servers(servers).annotations();

    route.getMetadata().getAnnotations().putAll(annotations);
  }

  private String getSecureProtocol(final String protocol) {
    if ("ws".equals(protocol)) {
      return "wss";
    } else if ("http".equals(protocol)) {
      return "https";
    } else return protocol;
  }

  private void enableTls(final Route route) {
    RouteSpec spec = route.getSpec();
    spec.setTls(getTLSConfig());
  }

  private TLSConfig getTLSConfig() {
    TLSConfig config = new TLSConfig();
    config.setTermination(TERMINATION_EDGE);
    config.setInsecureEdgeTerminationPolicy(TERMINATION_POLICY_REDIRECT);
    return config;
  }
}
