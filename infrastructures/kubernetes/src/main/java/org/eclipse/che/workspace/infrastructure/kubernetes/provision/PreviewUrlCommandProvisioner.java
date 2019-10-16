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

import static org.eclipse.che.api.core.model.workspace.config.Command.PREVIEW_URL_ATTRIBUTE;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates commands with proper Preview URL attribute.
 *
 * <p>Goes through all {@link CommandImpl}s in given {@link KubernetesEnvironment} and for ones that
 * have defined Preview URL tries to find matching {@link Service} and {@link Ingress} from given
 * {@link KubernetesNamespace}. When found, it composes full URL and set it as attribute of a
 * command with key {@link
 * org.eclipse.che.api.core.model.workspace.config.Command#PREVIEW_URL_ATTRIBUTE}.
 *
 * @param <E> type of the environment
 */
@Singleton
public class PreviewUrlCommandProvisioner<E extends KubernetesEnvironment> {

  private static final Logger LOG = LoggerFactory.getLogger(PreviewUrlCommandProvisioner.class);

  public void provision(E env, KubernetesNamespace namespace) throws InfrastructureException {
    injectsPreviewUrlToCommands(env, namespace);
  }

  /**
   * Go through all commands, find matching service and exposed host. Then construct full preview
   * url from this data and set it as Command's parameter under `previewUrl` key.
   *
   * @param env environment to get commands
   * @param namespace current kubernetes namespace where we're looking for services and ingresses
   */
  private void injectsPreviewUrlToCommands(E env, KubernetesNamespace namespace)
      throws InfrastructureException {
    for (CommandImpl command :
        env.getCommands()
            .stream()
            .filter(c -> c.getPreviewUrl() != null)
            .collect(Collectors.toList())) {
      Optional<Service> foundService =
          Services.findServiceWithPort(
              namespace.services().get(), command.getPreviewUrl().getPort());
      if (foundService.isPresent()) {
        Optional<String> foundHost =
            findHostForServicePort(
                namespace, foundService.get(), command.getPreviewUrl().getPort());
        if (foundHost.isPresent()) {
          command.getAttributes().put(PREVIEW_URL_ATTRIBUTE, foundHost.get());
        } else {
          LOG.warn(
              "unable to find ingress for service [{}] and port [{}]",
              foundService.get(),
              command.getPreviewUrl().getPort());
        }
      } else {
        LOG.warn(
            "unable to find service for port [{}] for command [{}]",
            command.getPreviewUrl().getPort(),
            command.getName());
      }
    }
  }

  protected Optional<String> findHostForServicePort(
      KubernetesNamespace namespace, Service service, int port) throws InfrastructureException {
    Optional<ServicePort> foundPort = Services.findPort(service, port);
    if (!foundPort.isPresent()) {
      return Optional.empty();
    }

    for (Ingress ingress : namespace.ingresses().get()) {
      for (IngressRule rule : ingress.getSpec().getRules()) {
        for (HTTPIngressPath path : rule.getHttp().getPaths()) {
          IngressBackend backend = path.getBackend();
          if (backend.getServiceName().equals(service.getMetadata().getName())
              && backend.getServicePort().getStrVal().equals(foundPort.get().getName())) {
            return Optional.of(rule.getHost());
          }
        }
      }
    }
    return Optional.empty();
  }
}
