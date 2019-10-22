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
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Ingresses;
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
    if (env.getCommands() == null) {
      return;
    }

    List<?> ingresses = loadIngresses(namespace);
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
                ingresses, foundService.get(), command.getPreviewUrl().getPort());
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

  protected List<?> loadIngresses(KubernetesNamespace namespace) throws InfrastructureException {
    return namespace.ingresses().get();
  }

  protected Optional<String> findHostForServicePort(
      List<?> ingressList, Service service, int port) {
    List<Ingress> ingresses =
        ingressList.stream().map(i -> (Ingress) i).collect(Collectors.toList());
    return Ingresses.findIngressRuleForServicePort(ingresses, service, port)
        .map(IngressRule::getHost);
  }
}
