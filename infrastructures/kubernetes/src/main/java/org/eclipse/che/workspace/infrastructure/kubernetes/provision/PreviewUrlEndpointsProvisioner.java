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

package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_PREFIX;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_UNIQUE_PART_SIZE;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.ServerServiceBuilder;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Services;

@Singleton
public class PreviewUrlEndpointsProvisioner<T extends KubernetesEnvironment>
    implements ConfigurationProvisioner<T> {

  private final ExternalServerExposer<T> externalServerExposer;

  @Inject
  public PreviewUrlEndpointsProvisioner(ExternalServerExposer<T> externalServerExposer) {
    this.externalServerExposer = externalServerExposer;
  }

  @Override
  public void provision(T k8sEnv, RuntimeIdentity identity) throws InfrastructureException {
    createEndpointsForPreviewUrls(k8sEnv);
  }

  private void createEndpointsForPreviewUrls(T env) {
    List<CommandImpl> previewUrlCommands =
        env.getCommands()
            .stream()
            .filter(c -> c.getPreviewUrl() != null)
            .collect(Collectors.toList());

    List<ServicePort> portsToProvision = new ArrayList<>();
    for (CommandImpl command : previewUrlCommands) {
      int port = command.getPreviewUrl().getPort();
      Optional<Service> foundService =
          Services.findServiceWithPort(env.getServices().values(), port);
      if (foundService.isPresent()) {
        if (!hasMatchingEndpoint(env, foundService.get(), port)) {
          externalServerExposer.expose(
              env,
              null,
              foundService.get().getMetadata().getName(),
              createServicePort(port),
              Collections.emptyMap());
        }
      } else {
        portsToProvision.add(createServicePort(port));
      }
    }

    if (!portsToProvision.isEmpty()) {
      Service service =
          new ServerServiceBuilder()
              .withName(generate(SERVER_PREFIX, SERVER_UNIQUE_PART_SIZE) + "-previewUrl")
              .withPorts(portsToProvision)
              .build();
      env.getServices().put(service.getMetadata().getName(), service);
      portsToProvision.forEach(
          port ->
              externalServerExposer.expose(
                  env, null, service.getMetadata().getName(), port, Collections.emptyMap()));
    }
  }

  private ServicePort createServicePort(int port) {
    return new ServicePort("server-" + port, null, port, "TCP", new IntOrString(port));
  }

  protected boolean hasMatchingEndpoint(T env, Service service, int port) {
    Optional<ServicePort> foundPort = Services.findPort(service, port);
    if (!foundPort.isPresent()) {
      return false;
    }

    for (Ingress ingress : env.getIngresses().values()) {
      for (IngressRule rule : ingress.getSpec().getRules()) {
        for (HTTPIngressPath path : rule.getHttp().getPaths()) {
          IngressBackend backend = path.getBackend();
          if (backend.getServiceName().equals(service.getMetadata().getName())
              && backend.getServicePort().getStrVal().equals(foundPort.get().getName())) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
