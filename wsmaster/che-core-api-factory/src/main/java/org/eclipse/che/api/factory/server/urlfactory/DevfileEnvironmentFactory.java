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
package org.eclipse.che.api.factory.server.urlfactory;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Creates workspace environment from specific tool in devfile if any.
 *
 * @author Max Shaposhnyk
 */
@Singleton
public class DevfileEnvironmentFactory {

  static final String DEFAULT_RECIPE_CONTENT_TYPE = "application/x-yaml";

  private final URLFetcher urlFetcher;

  @Inject
  public DevfileEnvironmentFactory(URLFetcher urlFetcher) {
    this.urlFetcher = urlFetcher;
  }

  /**
   * Finds an recipe-type tool (openshift or kubernetes) in devfile and tries to provision {@link
   * EnvironmentImpl} from it. If such tool is present in devfile, an file URL composer function
   * MUST be provided to allow to fetch recipe content.
   *
   * @param devfile source devfile
   * @param fileUrlComposer optional service-specific composer of URL's to the file raw content
   * @return constructed environment
   * @throws BadRequestException when there is no URL provider for recipe-type tool present in
   *     devfile
   * @throws BadRequestException when recipe-type tool content is unreachable or empty
   */
  public Optional<EnvironmentImpl> tryProvision(
      Devfile devfile, @Nullable Function<String, String> fileUrlComposer)
      throws BadRequestException {
    List<Tool> recipeToolList =
        devfile
            .getTools()
            .stream()
            .filter(
                tool ->
                    tool.getType().equals(KUBERNETES_TOOL_TYPE)
                        || tool.getType().equals(OPENSHIFT_TOOL_TYPE))
            .collect(toList());
    if (recipeToolList.isEmpty()) {
      return Optional.empty();
    }
    if (recipeToolList.size() > 1) {
      throw new BadRequestException(
          format(
              "Multiple non plugin or editor type tools found (%d) but expected only one.",
              recipeToolList.size()));
    }
    final Tool recipeTool = recipeToolList.get(0);
    final String type = recipeTool.getType();
    if (fileUrlComposer == null) {
      throw new BadRequestException(
          format("This kind of factory URL's does not support '%s' type tools.", type));
    }

    String localFileContent = urlFetcher.fetch(fileUrlComposer.apply(recipeTool.getLocal()));
    if (isNullOrEmpty(localFileContent)) {
      throw new BadRequestException(
          format(
              "The local file '%s' defined in tool  '%s' is unreachable or empty.",
              recipeTool.getLocal(), recipeTool.getName()));
    }
    // TODO: it would be great to check there is real yaml and not binary etc
    RecipeImpl recipe = new RecipeImpl(type, DEFAULT_RECIPE_CONTENT_TYPE, localFileContent, null);
    return Optional.of(new EnvironmentImpl(recipe, emptyMap()));
  }
}
