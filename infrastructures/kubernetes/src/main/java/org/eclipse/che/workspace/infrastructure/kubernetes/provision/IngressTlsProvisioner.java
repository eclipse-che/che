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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.TlsProvisioner.getSecureProtocol;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressTLS;
import io.fabric8.kubernetes.api.model.extensions.IngressTLSBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Enables Transport Layer Security (TLS) for external server ingresses
 *
 * @author Guy Daich
 */
@Singleton
public class IngressTlsProvisioner
    implements ConfigurationProvisioner<KubernetesEnvironment>,
        TlsProvisioner<KubernetesEnvironment> {
  static final String TLS_SECRET_TYPE = "kubernetes.io/tls";

  private final boolean isTlsEnabled;
  private final String tlsSecretName;
  private final String tlsCert;
  private final String tlsKey;

  @Inject
  public IngressTlsProvisioner(
      @Named("che.infra.kubernetes.tls_enabled") boolean isTlsEnabled,
      @Named("che.infra.kubernetes.tls_secret") String tlsSecretName,
      @Nullable @Named("che.infra.kubernetes.tls_cert") String tlsCert,
      @Nullable @Named("che.infra.kubernetes.tls_key") String tlsKey) {
    this.isTlsEnabled = isTlsEnabled;
    this.tlsSecretName = tlsSecretName;

    if (isNullOrEmpty(tlsCert) != isNullOrEmpty(tlsKey)) {
      throw new ConfigurationException(
          "None or both of `che.infra.kubernetes.tls_cert` and "
              + "`che.infra.kubernetes.tls_key` must be configured with non-null value.");
    }
    this.tlsCert = tlsCert;
    this.tlsKey = tlsKey;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws KubernetesInfrastructureException {
    if (!isTlsEnabled) {
      return;
    }

    String wsTlsSecretName = tlsSecretName;

    if (!isNullOrEmpty(tlsCert) && !isNullOrEmpty(tlsKey)) {
      wsTlsSecretName = identity.getWorkspaceId() + '-' + tlsSecretName;
      provisionTlsSecret(k8sEnv, wsTlsSecretName);
    }

    for (Ingress ingress : k8sEnv.getIngresses().values()) {
      useSecureProtocolForIngressServers(ingress);
      enableTLS(ingress, wsTlsSecretName);
    }
  }

  private void provisionTlsSecret(KubernetesEnvironment k8sEnv, String wsTlsSecretName) {
    Map<String, String> data = new HashMap<>();
    data.put("tls.crt", tlsCert);
    data.put("tls.key", tlsKey);

    Secret tlsSecret =
        new SecretBuilder()
            .withNewMetadata()
            .withName(wsTlsSecretName)
            .endMetadata()
            .withStringData(data)
            .withType(TLS_SECRET_TYPE)
            .build();

    k8sEnv.getSecrets().put(wsTlsSecretName, tlsSecret);
  }

  private void enableTLS(Ingress ingress, String wsTlsSecretName) {
    String host = ingress.getSpec().getRules().get(0).getHost();

    IngressTLSBuilder ingressTLSBuilder = new IngressTLSBuilder().withHosts(host);

    // according to ingress tls spec, secret name is optional
    // when working in single-host mode, nginx controller wil reuse the che-master secret
    // https://github.com/kubernetes/kubernetes/blob/master/staging/src/k8s.io/api/extensions/v1beta1/types.go
    if (!isNullOrEmpty(wsTlsSecretName)) {
      ingressTLSBuilder.withSecretName(wsTlsSecretName);
    }

    IngressTLS ingressTLS = ingressTLSBuilder.build();
    List<IngressTLS> ingressTLSList = new ArrayList<>(Collections.singletonList(ingressTLS));
    ingress.getSpec().setTls(ingressTLSList);
  }

  private void useSecureProtocolForIngressServers(final Ingress ingress) {
    Map<String, ServerConfigImpl> servers =
        Annotations.newDeserializer(ingress.getMetadata().getAnnotations()).servers();

    if (servers.isEmpty()) {
      return;
    }

    servers.values().forEach(s -> s.setProtocol(getSecureProtocol(s.getProtocol())));

    Map<String, String> annotations = Annotations.newSerializer().servers(servers).annotations();

    ingress.getMetadata().getAnnotations().putAll(annotations);
  }
}
