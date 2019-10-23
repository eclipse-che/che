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
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.NOT_ABLE_TO_PROVISION_OBJECTS_FOR_PREVIEW_URL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.NOT_ABLE_TO_PROVISION_OBJECTS_FOR_PREVIEW_URL_MESSAGE;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
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
    List<Service> services = loadServices(namespace);
    for (CommandImpl command :
        env.getCommands()
            .stream()
            .filter(c -> c.getPreviewUrl() != null)
            .collect(Collectors.toList())) {
      Optional<Service> foundService =
          Services.findServiceWithPort(services, command.getPreviewUrl().getPort());
      if (!foundService.isPresent()) {
        String message =
            String.format(
                "unable to find service for port '%s' for command '%s'",
                command.getPreviewUrl().getPort(), command.getName());
        LOG.warn(message);
        env.addWarning(
            new WarningImpl(
                NOT_ABLE_TO_PROVISION_OBJECTS_FOR_PREVIEW_URL,
                String.format(NOT_ABLE_TO_PROVISION_OBJECTS_FOR_PREVIEW_URL_MESSAGE, message)));
        continue;
      }

      Optional<String> foundHost =
          findHostForServicePort(ingresses, foundService.get(), command.getPreviewUrl().getPort());
      if (foundHost.isPresent()) {
        command.getAttributes().put(PREVIEW_URL_ATTRIBUTE, foundHost.get());
      } else {
        String message =
            String.format(
                "unable to find ingress for service '%s' and port '%s'",
                foundService.get(), command.getPreviewUrl().getPort());
        LOG.warn(message);
        env.addWarning(
            new WarningImpl(
                NOT_ABLE_TO_PROVISION_OBJECTS_FOR_PREVIEW_URL,
                String.format(NOT_ABLE_TO_PROVISION_OBJECTS_FOR_PREVIEW_URL_MESSAGE, message)));
      }
    }
  }

  private List<Service> loadServices(KubernetesNamespace namespace) throws InfrastructureException {
    return namespace.services().get();
  }

  protected List<?> loadIngresses(KubernetesNamespace namespace) throws InfrastructureException {
    return namespace.ingresses().get();
  }

  protected Optional<String> findHostForServicePort(List<?> ingressList, Service service, int port)
      throws InternalInfrastructureException {
    final List<Ingress> ingresses;
    try {
      ingresses = ingressList.stream().map(i -> (Ingress) i).collect(Collectors.toList());
    } catch (ClassCastException cce) {
      throw new InternalInfrastructureException(
          "Failed casting to Kubernetes Ingress. This is not expected. Please report a bug!");
    }

    return Ingresses.findIngressRuleForServicePort(ingresses, service, port)
        .map(IngressRule::getHost);
  }
}
