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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putAnnotations;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.TlsProvisioner.getSecureProtocol;

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
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TlsProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * Enables Transport Layer Security (TLS) for workspace routes and changes protocol to secure (wss /
 * https) in servers' configuration
 *
 * @author Ilya Buziuk
 */
@Singleton
public class RouteTlsProvisioner
    implements TlsProvisioner<OpenShiftEnvironment>,
        ConfigurationProvisioner<OpenShiftEnvironment> {

  static final String TERMINATION_EDGE = "edge";
  static final String TERMINATION_POLICY_REDIRECT = "Redirect";
  private final boolean isTlsEnabled;

  @Inject
  public RouteTlsProvisioner(@Named("che.infra.kubernetes.tls_enabled") boolean isTlsEnabled) {
    this.isTlsEnabled = isTlsEnabled;
  }

  @Override
  @Traced
  public void provision(OpenShiftEnvironment osEnv, RuntimeIdentity identity) {
    TracingTags.WORKSPACE_ID.set(identity::getWorkspaceId);

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
    putAnnotations(route.getMetadata(), annotations);
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
