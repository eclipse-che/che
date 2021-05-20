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
package org.eclipse.che.api.factory.server.urlfactory;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.api.workspace.server.devfile.Constants.CURRENT_API_VERSION;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.UnknownScmProviderException;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl.DevfileLocation;
import org.eclipse.che.api.factory.shared.dto.FactoryDevfileV2Dto;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.factory.shared.dto.FactoryMetaDto;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.devfile.DevfileParser;
import org.eclipse.che.api.workspace.server.devfile.DevfileVersionDetector;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.OverrideParameterException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.MetadataDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the creation of some elements used inside a {@link FactoryDto}.
 *
 * @author Florent Benoit
 * @author Max Shaposhnyk
 */
@Singleton
public class URLFactoryBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(URLFactoryBuilder.class);

  private final String defaultCheEditor;
  private final String defaultChePlugins;

  private final DevfileParser devfileParser;
  private final DevfileVersionDetector devfileVersionDetector;

  @Inject
  public URLFactoryBuilder(
      @Named("che.factory.default_editor") String defaultCheEditor,
      @Nullable @Named("che.factory.default_plugins") String defaultChePlugins,
      DevfileParser devfileParser,
      DevfileVersionDetector devfileVersionDetector) {
    this.defaultCheEditor = defaultCheEditor;
    this.defaultChePlugins = defaultChePlugins;
    this.devfileParser = devfileParser;
    this.devfileVersionDetector = devfileVersionDetector;
  }

  /**
   * Build a factory using the provided devfile. Allows to override devfile properties using
   * specially constructed map {@see DevfileManager#parseYaml(String, Map)}.
   *
   * <p>We want factory to never fail due to name collision. Taking `generateName` with precedence.
   * <br>
   * If devfile has only `name`, we convert it to `generateName`. <br>
   * If devfile has `name` and `generateName`, we remove `name` and use just `generateName`. <br>
   * If devfile has `generateName`, we use that.
   *
   * @param remoteFactoryUrl parsed factory URL object
   * @param fileContentProvider service-specific devfile related file content provider
   * @param overrideProperties map of overridden properties to apply in devfile
   * @return a factory or null if devfile is not found
   */
  public Optional<FactoryMetaDto> createFactoryFromDevfile(
      RemoteFactoryUrl remoteFactoryUrl,
      FileContentProvider fileContentProvider,
      Map<String, String> overrideProperties)
      throws ApiException {
    String devfileYamlContent;
    for (DevfileLocation location : remoteFactoryUrl.devfileFileLocations()) {
      try {
        devfileYamlContent = fileContentProvider.fetchContent(location.location());
      } catch (IOException ex) {
        // try next location
        LOG.debug(
            "Unreachable devfile location met: {}. Error is: {}",
            location.location(),
            ex.getMessage());
        continue;
      } catch (DevfileException e) {
        LOG.debug("Unexpected devfile exception: {}", e.getMessage());
        throw toApiException(e, location);
      }
      if (isNullOrEmpty(devfileYamlContent)) {
        return Optional.empty();
      }

      try {
        JsonNode parsedDevfile = devfileParser.parseYamlRaw(devfileYamlContent);
        return Optional.of(
            createFactory(parsedDevfile, overrideProperties, fileContentProvider, location));
      } catch (OverrideParameterException e) {
        throw new BadRequestException("Error processing override parameter(s): " + e.getMessage());
      } catch (DevfileException e) {
        throw toApiException(e, location);
      }
    }
    return Optional.empty();
  }

  private ApiException toApiException(DevfileException devfileException, DevfileLocation location) {
    Throwable cause = devfileException.getCause();
    if (cause instanceof ScmUnauthorizedException) {
      ScmUnauthorizedException scmCause = (ScmUnauthorizedException) cause;
      return new UnauthorizedException(
          "SCM Authentication required",
          401,
          Map.of(
              "oauth_version", scmCause.getOauthVersion(),
              "oauth_provider", scmCause.getOauthProvider(),
              "oauth_authentication_url", scmCause.getAuthenticateUrl()));
    } else if (cause instanceof UnknownScmProviderException) {
      return new ServerException(
          "Provided location is unknown or misconfigured on the server side. Error message:"
              + cause.getMessage());
    } else if (cause instanceof ScmCommunicationException) {
      return new ServerException(
          "There is an error happened when communicate with SCM server. Error message:"
              + cause.getMessage());
    }
    return new BadRequestException(
        "Error occurred during creation a workspace from devfile located at `"
            + location.location()
            + "`. Cause: "
            + devfileException.getMessage());
  }

  /**
   * Converts given devfile json into factory based on the devfile version.
   *
   * @param overrideProperties map of overridden properties to apply in devfile
   * @param fileContentProvider service-specific devfile related file content provider
   * @param location devfile's location
   * @return new factory created from the given devfile
   * @throws OverrideParameterException when any issue when overriding parameters occur
   * @throws DevfileException when devfile is not valid or we can't work with it
   */
  private FactoryMetaDto createFactory(
      JsonNode devfileJson,
      Map<String, String> overrideProperties,
      FileContentProvider fileContentProvider,
      DevfileLocation location)
      throws OverrideParameterException, DevfileException {

    if (devfileVersionDetector.devfileMajorVersion(devfileJson) == 1) {
      DevfileImpl devfile = devfileParser.parseJsonNode(devfileJson, overrideProperties);
      devfileParser.resolveReference(devfile, fileContentProvider);
      devfile = ensureToUseGenerateName(devfile);

      return newDto(FactoryDto.class)
          .withV(CURRENT_VERSION)
          .withDevfile(DtoConverter.asDto(devfile))
          .withSource(location.filename().isPresent() ? location.filename().get() : null);

    } else {
      return newDto(FactoryDevfileV2Dto.class)
          .withV(CURRENT_VERSION)
          .withDevfile(devfileParser.convertYamlToMap(devfileJson))
          .withSource(location.filename().isPresent() ? location.filename().get() : null);
    }
  }

  /**
   * Creates devfile with only `generateName` and no `name`. We take `generateName` with precedence.
   * See doc of {@link URLFactoryBuilder#createFactoryFromDevfile(RemoteFactoryUrl,
   * FileContentProvider, Map)} for explanation why.
   */
  private DevfileImpl ensureToUseGenerateName(DevfileImpl devfile) {
    MetadataImpl devfileMetadata = new MetadataImpl(devfile.getMetadata());
    if (isNullOrEmpty(devfileMetadata.getGenerateName())) {
      devfileMetadata.setGenerateName(devfileMetadata.getName());
    }
    devfileMetadata.setName(null);

    DevfileImpl devfileWithProperName = new DevfileImpl(devfile);
    devfileWithProperName.setMetadata(devfileMetadata);
    return devfileWithProperName;
  }

  /**
   * Help to generate default workspace configuration
   *
   * @param name the name of the workspace
   * @return a workspace configuration
   */
  public WorkspaceConfigDto buildDefaultWorkspaceConfig(String name) {

    Map<String, String> attributes = new HashMap<>();
    attributes.put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, defaultCheEditor);
    if (!isNullOrEmpty(defaultChePlugins)) {
      attributes.put(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, defaultChePlugins);
    }

    // workspace configuration using the environment
    return newDto(WorkspaceConfigDto.class).withName(name).withAttributes(attributes);
  }

  /**
   * Help to generate default workspace devfile. Also initialise project in it
   *
   * @param name the name that will be used as `generateName` in the devfile
   * @return a workspace devfile
   */
  public DevfileDto buildDefaultDevfile(String name) {

    // workspace configuration using the environment
    return newDto(DevfileDto.class)
        .withApiVersion(CURRENT_API_VERSION)
        .withMetadata(newDto(MetadataDto.class).withGenerateName(name));
  }
}
