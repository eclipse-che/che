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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static java.lang.String.format;
import static org.eclipse.che.api.workspace.server.devfile.Components.getIdentifiableComponentName;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Entrypoint;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.validator.ComponentIntegrityValidator;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;

public class KubernetesComponentValidator implements ComponentIntegrityValidator {
  private final KubernetesRecipeParser kubernetesRecipeParser;
  private final Set<String> k8sBasedComponentTypes;

  @Inject
  public KubernetesComponentValidator(
      KubernetesRecipeParser kubernetesRecipeParser,
      @Named(KubernetesDevfileBindings.KUBERNETES_BASED_COMPONENTS_KEY_NAME)
          Set<String> k8sBasedComponentTypes) {
    this.kubernetesRecipeParser = kubernetesRecipeParser;
    this.k8sBasedComponentTypes = k8sBasedComponentTypes;
  }

  @Override
  public void validateComponent(Component component, FileContentProvider contentProvider)
      throws DevfileFormatException {
    try {
      List<HasMetadata> selectedObjects = validateSelector(component, contentProvider);
      validateEntrypointSelector(component, selectedObjects);
    } catch (Exception e) {
      throw new DevfileFormatException(
          format(
              "Failed to validate content reference of component '%s' of type '%s': %s",
              getIdentifiableComponentName(component), component.getType(), e.getMessage()),
          e);
    }
  }

  /**
   * Validates that the selector, if any, selects some objects from the component's referenced
   * content. Only does anything for kubernetes and openshift components.
   *
   * @param component the component to check
   * @param contentProvider the content provider to use when fetching content
   * @return the list of referenced objects matching the selector or empty list
   * @throws ValidationException on failure to validate the referenced content
   * @throws InfrastructureException on failure to parse the referenced content
   * @throws IOException on failure to retrieve the referenced content
   * @throws DevfileException if the selector filters out all referenced objects
   */
  private List<HasMetadata> validateSelector(
      Component component, FileContentProvider contentProvider)
      throws ValidationException, InfrastructureException, IOException, DevfileException {

    if (!k8sBasedComponentTypes.contains(component.getType())) {
      return Collections.emptyList();
    }

    List<HasMetadata> content = getReferencedKubernetesList(component, contentProvider);

    Map<String, String> selector = component.getSelector();
    if (selector == null || selector.isEmpty()) {
      return content;
    }

    content = SelectorFilter.filter(content, selector);

    if (content.isEmpty()) {
      throw new DevfileException(
          format(
              "The selector of the component '%s' of type '%s' filters out all objects from"
                  + " the list.",
              getIdentifiableComponentName(component), component.getType()));
    }

    return content;
  }

  private void validateEntrypointSelector(Component component, List<HasMetadata> filteredObjects)
      throws DevfileException {

    if (component.getEntrypoints() == null || component.getEntrypoints().isEmpty()) {
      return;
    }

    for (Entrypoint ep : component.getEntrypoints()) {
      ContainerSearch search =
          new ContainerSearch(ep.getParentName(), ep.getParentSelector(), ep.getContainerName());

      List<Container> cs = search.search(filteredObjects);

      if (cs.isEmpty()) {
        throw new DevfileFormatException(
            format(
                "Component '%s' of type '%s' contains an entry point that doesn't match any"
                    + " container.",
                getIdentifiableComponentName(component), component.getType()));
      }
    }
  }

  private List<HasMetadata> getReferencedKubernetesList(
      Component component, FileContentProvider contentProvider)
      throws ValidationException, InfrastructureException, IOException, DevfileException {
    List<HasMetadata> content;
    if (component.getReferenceContent() != null) {
      content = kubernetesRecipeParser.parse(component.getReferenceContent());
    } else if (component.getReference() != null) {
      String data = contentProvider.fetchContent(component.getReference());
      content = kubernetesRecipeParser.parse(data);
    } else {
      content = Collections.emptyList();
    }

    return content;
  }
}
