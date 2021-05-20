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
package org.eclipse.che.api.factory.server.bitbucket;

import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.api.factory.shared.Constants.URL_PARAMETER_NAME;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.server.DefaultFactoryParameterResolver;
import org.eclipse.che.api.factory.server.scm.GitCredentialManager;
import org.eclipse.che.api.factory.server.scm.PersonalAccessTokenManager;
import org.eclipse.che.api.factory.server.urlfactory.URLFactoryBuilder;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.factory.shared.dto.FactoryMetaDto;
import org.eclipse.che.api.factory.shared.dto.FactoryVisitor;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.shared.dto.devfile.ProjectDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.SourceDto;

/**
 * Provides Factory Parameters resolver for both public and private bitbucket repositories.
 *
 * @author Max Shaposhnyk
 */
@Singleton
public class BitbucketServerAuthorizingFactoryParametersResolver
    extends DefaultFactoryParameterResolver {

  /** Parser which will allow to check validity of URLs and create objects. */
  private final BitbucketURLParser bitbucketURLParser;

  private final GitCredentialManager gitCredentialManager;
  private final PersonalAccessTokenManager personalAccessTokenManager;

  @Inject
  public BitbucketServerAuthorizingFactoryParametersResolver(
      URLFactoryBuilder urlFactoryBuilder,
      URLFetcher urlFetcher,
      BitbucketURLParser bitbucketURLParser,
      GitCredentialManager gitCredentialManager,
      PersonalAccessTokenManager personalAccessTokenManager) {
    super(urlFactoryBuilder, urlFetcher);
    this.bitbucketURLParser = bitbucketURLParser;
    this.gitCredentialManager = gitCredentialManager;
    this.personalAccessTokenManager = personalAccessTokenManager;
  }

  /**
   * Check if this resolver can be used with the given parameters.
   *
   * @param factoryParameters map of parameters dedicated to factories
   * @return true if it will be accepted by the resolver implementation or false if it is not
   *     accepted
   */
  @Override
  public boolean accept(@NotNull final Map<String, String> factoryParameters) {
    return factoryParameters.containsKey(URL_PARAMETER_NAME)
        && bitbucketURLParser.isValid(factoryParameters.get(URL_PARAMETER_NAME));
  }

  /**
   * Create factory object based on provided parameters
   *
   * @param factoryParameters map containing factory data parameters provided through URL
   * @throws BadRequestException when data are invalid
   */
  @Override
  public FactoryMetaDto createFactory(@NotNull final Map<String, String> factoryParameters)
      throws ApiException {

    // no need to check null value of url parameter as accept() method has performed the check
    final BitbucketUrl bitbucketUrl =
        bitbucketURLParser.parse(factoryParameters.get(URL_PARAMETER_NAME));

    final FileContentProvider fileContentProvider =
        new BitbucketServerAuthorizingFileContentProvider(
            bitbucketUrl, urlFetcher, gitCredentialManager, personalAccessTokenManager);

    // create factory from the following location if location exists, else create default factory
    return urlFactoryBuilder
        .createFactoryFromDevfile(
            bitbucketUrl, fileContentProvider, extractOverrideParams(factoryParameters))
        .orElseGet(() -> newDto(FactoryDto.class).withV(CURRENT_VERSION).withSource("repo"))
        .acceptVisitor(new BitbucketFactoryVisitor(bitbucketUrl));
  }

  /**
   * Visitor that puts the default devfile or updates devfile projects into the Bitbucket Factory,
   * if needed.
   */
  private class BitbucketFactoryVisitor implements FactoryVisitor {

    private final BitbucketUrl bitbucketUrl;

    private BitbucketFactoryVisitor(BitbucketUrl bitbucketUrl) {
      this.bitbucketUrl = bitbucketUrl;
    }

    @Override
    public FactoryDto visit(FactoryDto factory) {
      if (factory.getDevfile() == null) {
        // initialize default devfile
        factory.setDevfile(urlFactoryBuilder.buildDefaultDevfile(bitbucketUrl.getRepository()));
      }

      updateProjects(
          factory.getDevfile(),
          () ->
              newDto(ProjectDto.class)
                  .withSource(
                      newDto(SourceDto.class)
                          .withLocation(bitbucketUrl.repositoryLocation())
                          .withType("git")
                          .withBranch(bitbucketUrl.getBranch()))
                  .withName(bitbucketUrl.getRepository()),
          project -> {
            final String location = project.getSource().getLocation();
            if (location.equals(bitbucketUrl.repositoryLocation())) {
              project.getSource().setBranch(bitbucketUrl.getBranch());
            }
          });

      return factory;
    }
  }
}
