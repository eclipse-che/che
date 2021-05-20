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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import com.google.common.collect.ImmutableSet;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;

/**
 * Parses Kubernetes objects from recipe.
 *
 * <p>Note that this class can also parse OpenShift specific objects.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesRecipeParser {

  private static final Set<String> SUPPORTED_CONTENT_TYPES =
      ImmutableSet.of("application/x-yaml", "text/yaml", "text/x-yaml");

  private final KubernetesClientFactory clientFactory;

  @Inject
  public KubernetesRecipeParser(KubernetesClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  /**
   * Parses Kubernetes objects from recipe.
   *
   * @param recipe that contains objects to parse
   * @return parsed objects
   * @throws IllegalArgumentException if recipe content is null
   * @throws IllegalArgumentException if recipe content type is null
   * @throws ValidationException if recipe content has broken format
   * @throws ValidationException if recipe content has unrecognized objects
   * @throws InfrastructureException when exception occurred during kubernetes client creation
   */
  public List<HasMetadata> parse(InternalRecipe recipe)
      throws ValidationException, InfrastructureException {
    String content = recipe.getContent();
    String contentType = recipe.getContentType();
    checkNotNull(contentType, "Recipe content type must not be null");

    if (!SUPPORTED_CONTENT_TYPES.contains(contentType)) {
      throw new ValidationException(
          format(
              "Provided environment recipe content type '%s' is unsupported. Supported values are: %s",
              contentType, String.join(", ", SUPPORTED_CONTENT_TYPES)));
    }

    return parse(content);
  }

  /**
   * Parses Kubernetes objects from recipe content.
   *
   * @param recipeContent recipe content that should be parsed
   * @return parsed objects
   * @throws IllegalArgumentException if recipe content is null
   * @throws ValidationException if recipe content has broken format
   * @throws ValidationException if recipe content has unrecognized objects
   * @throws InfrastructureException when exception occurred during kubernetes client creation
   */
  public List<HasMetadata> parse(String recipeContent)
      throws ValidationException, InfrastructureException {
    checkNotNull(recipeContent, "Recipe content type must not be null");

    try {
      // Behavior:
      // - If `content` is a single object like Deployment, load().get() will get the object in that
      // list
      // - If `content` is a Kubernetes List, load().get() will get the objects in that list
      // - If `content` is an OpenShift template, load().get() will get the objects in the template
      //   with parameters substituted (e.g. with default values).
      List<HasMetadata> parsed =
          clientFactory.create().load(new ByteArrayInputStream(recipeContent.getBytes())).get();

      // needed because Che master namespace is set by K8s API during list loading
      parsed
          .stream()
          .filter(o -> o.getMetadata() != null)
          .forEach(o -> o.getMetadata().setNamespace(null));

      return parsed;
    } catch (KubernetesClientException e) {
      // KubernetesClient wraps the error when a JsonMappingException occurs so we need the cause
      String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
      if (message.contains("\n")) {
        // Clean up message if it comes from JsonMappingException. Format is e.g.
        // `No resource type found for:v1#Route1\n at [...]`
        message = message.split("\\n", 2)[0];
      }
      throw new ValidationException(format("Could not parse Kubernetes recipe: %s", message));
    }
  }
}
