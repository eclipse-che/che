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
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_PREFIX;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_UNIQUE_PART_SIZE;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Ingresses;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Services;

/**
 * For Commands that have defined Preview URL, tries to find existing {@link Service} and {@link
 * Ingress}. When not found, we create new ones and put them into given {@link
 * KubernetesEnvironment}.
 *
 * @param <T> type of the environment
 */
@Singleton
public class PreviewUrlExposer<T extends KubernetesEnvironment> {

  private final ExternalServerExposer<T> externalServerExposer;

  @Inject
  public PreviewUrlExposer(ExternalServerExposerProvider<T> externalServerExposer) {
    this(externalServerExposer.get());
  }

  @VisibleForTesting
  protected PreviewUrlExposer(ExternalServerExposer<T> externalServerExposer) {
    this.externalServerExposer = externalServerExposer;
  }

  public void expose(T env) throws InternalInfrastructureException {
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
          ServicePort servicePort =
              Services.findPort(foundService.get(), port)
                  .orElseThrow(
                      () ->
                          new InternalInfrastructureException(
                              String.format(
                                  "Port '%d' in service '%s' not found. This is not expected, please report a bug!",
                                  port, foundService.get().getMetadata().getName())));
          String serviceName = foundService.get().getMetadata().getName();
          externalServerExposer.expose(
              env, null, serviceName, serviceName, servicePort, Collections.emptyMap());
        }
      } else {
        portsToProvision.add(createServicePort(port));
      }
    }

    if (!portsToProvision.isEmpty()) {
      String serverName = generate(SERVER_PREFIX, SERVER_UNIQUE_PART_SIZE) + "-previewUrl";
      Service service =
          new ServerServiceBuilder().withName(serverName).withPorts(portsToProvision).build();
      env.getServices().put(serverName, service);
      portsToProvision.forEach(
          port ->
              externalServerExposer.expose(
                  env,
                  null,
                  service.getMetadata().getName(),
                  service.getMetadata().getName(),
                  port,
                  Collections.emptyMap()));
    }
  }

  private ServicePort createServicePort(int port) {
    return new ServicePort(null, "server-" + port, null, port, "TCP", new IntOrString(port));
  }

  protected boolean hasMatchingEndpoint(T env, Service service, int port) {
    return Ingresses.findIngressRuleForServicePort(env.getIngresses().values(), service, port)
        .isPresent();
  }
}
