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
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;

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
 */
@Singleton
public class DevfileEnvironmentProvisioner {

  private final URLFetcher urlFetcher;

  @Inject
  public DevfileEnvironmentProvisioner(URLFetcher urlFetcher) {
    this.urlFetcher = urlFetcher;
  }

  /**
   * Finds an recipe-type tool (openshift or kubernetes) in devfile and
   * tries to provision {@link EnvironmentImpl} from it. If such tools are present in devfile,
   * an file URL composer function MUST be provided to allow to fetch recipe content.
   *
   * @param devfile source devfile
   * @param fileUrlComposer optional service-specific composer of URL's to the file raw content
   * @return provisioned enviromnent
   * @throws BadRequestException
   *         when there is recipe-type tool present in devfile but no URL composer provided
   *         when file specified in local section of the tool is unreachable or empty
   */
  public Optional<EnvironmentImpl> tryProvision(
      Devfile devfile, @Nullable Function<String, String> fileUrlComposer)
      throws BadRequestException {
    Optional<Tool> recipeToolOptional =
        devfile
            .getTools()
            .stream()
            .filter(
                tool ->
                    tool.getType().equals(KUBERNETES_TOOL_TYPE)
                        || tool.getType().equals(OPENSHIFT_TOOL_TYPE))
            .findFirst();
    if (!recipeToolOptional.isPresent()) {
      return Optional.empty();
    }
    final Tool recipeTool = recipeToolOptional.get();
    final String type = recipeTool.getType();
    if (fileUrlComposer == null) {
      throw new BadRequestException(
          "This kind of URL's does not support '" + type + "' type tools.");
    }

    String localFileContent = urlFetcher.fetch(fileUrlComposer.apply(recipeTool.getLocal()));
    if (isNullOrEmpty(localFileContent)) {
      throw new BadRequestException(
          "The local file '"
              + recipeTool.getLocal()
              + "' defined in tool  '"
              + recipeTool.getName()
              + "' is unreachable or empty.");
    }
    // TODO: it would be great to check there is real yaml and not binary etc
    RecipeImpl recipe = new RecipeImpl(type, "application/x-yaml", localFileContent, null);
    return Optional.of(new EnvironmentImpl(recipe, emptyMap()));
  }
}
