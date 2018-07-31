/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static com.google.common.base.Strings.isNullOrEmpty;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressTLS;
import io.fabric8.kubernetes.api.model.extensions.IngressTLSBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Enables Transport Layer Security (TLS) for external server ingresses
 *
 * @author Guy Daich
 */
public class IngressTlsProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  private final boolean isTlsEnabled;
  private final String tlsSecretName;

  @Inject
  public IngressTlsProvisioner(
      @Named("che.infra.kubernetes.tls_enabled") boolean isTlsEnabled,
      @Named("che.infra.kubernetes.tls_secret") String tlsSecretName) {
    this.isTlsEnabled = isTlsEnabled;
    this.tlsSecretName = tlsSecretName;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws KubernetesInfrastructureException {
    if (!isTlsEnabled) {
      return;
    }

    Collection<Ingress> ingresses = k8sEnv.getIngresses().values();
    for (Ingress ingress : ingresses) {
      useSecureProtocolForServers(ingress);
      enableTLS(ingress);
    }
  }

  private void enableTLS(Ingress ingress) {
    String host = ingress.getSpec().getRules().get(0).getHost();

    IngressTLSBuilder ingressTLSBuilder = new IngressTLSBuilder().withHosts(host);

    // according to ingress tls spec, secret name is optional
    // when working in single-host mode, nginx controller wil reuse the che-master secret
    // https://github.com/kubernetes/kubernetes/blob/master/staging/src/k8s.io/api/extensions/v1beta1/types.go
    if (!isNullOrEmpty(tlsSecretName)) {
      ingressTLSBuilder.withSecretName(tlsSecretName);
    }

    IngressTLS ingressTLS = ingressTLSBuilder.build();
    List<IngressTLS> ingressTLSList = new ArrayList<>(Collections.singletonList(ingressTLS));
    ingress.getSpec().setTls(ingressTLSList);
  }

  private void useSecureProtocolForServers(final Ingress ingress) {
    Map<String, ServerConfigImpl> servers =
        Annotations.newDeserializer(ingress.getMetadata().getAnnotations()).servers();

    servers.values().forEach(s -> s.setProtocol(getSecureProtocol(s.getProtocol())));

    Map<String, String> annotations = Annotations.newSerializer().servers(servers).annotations();

    ingress.getMetadata().getAnnotations().putAll(annotations);
  }

  private String getSecureProtocol(final String protocol) {
    if ("ws".equals(protocol)) {
      return "wss";
    } else if ("http".equals(protocol)) {
      return "https";
    } else return protocol;
  }
}
