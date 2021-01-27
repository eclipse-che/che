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
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl.DevfileLocation;
import org.eclipse.che.api.factory.shared.dto.FactoryDevfileV2Dto;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.factory.shared.dto.FactoryMetaDto;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.devfile.DevfileParser;
import org.eclipse.che.api.workspace.server.devfile.DevfileVersion;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.OverrideParameterException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.MetadataDto;
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
  private final DevfileVersion devfileVersion;

  @Inject
  public URLFactoryBuilder(
      @Named("che.factory.default_editor") String defaultCheEditor,
      @Named("che.factory.default_plugins") String defaultChePlugins,
      DevfileParser devfileParser,
      DevfileVersion devfileVersion) {
    this.defaultCheEditor = defaultCheEditor;
    this.defaultChePlugins = defaultChePlugins;
    this.devfileParser = devfileParser;
    this.devfileVersion = devfileVersion;
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
      throws BadRequestException {
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
        throw new BadRequestException(
            format(
                "There is an error resolving defvile. Error: %s. URL is %s",
                e.getMessage(), location.location()));
      }
      if (isNullOrEmpty(devfileYamlContent)) {
        return Optional.empty();
      }

      try {
        JsonNode parsedDevfile = devfileParser.parseRaw(devfileYamlContent);
        return Optional.of(
            convertToFactory(parsedDevfile, overrideProperties, fileContentProvider, location));
      } catch (DevfileException | OverrideParameterException e) {
        throw new BadRequestException(
            "Error occurred during creation a workspace from devfile located at `"
                + location.location()
                + "`. Cause: "
                + e.getMessage());
      }
    }
    return Optional.empty();
  }

  private FactoryMetaDto convertToFactory(
      JsonNode devfileJson,
      Map<String, String> overrideProperties,
      FileContentProvider fileContentProvider,
      DevfileLocation location)
      throws OverrideParameterException, DevfileException {

    if (devfileVersion.devfileMajorVersion(devfileJson) == 1) {
      DevfileImpl devfile = devfileParser.parseJsonNode(devfileJson, overrideProperties);
      devfileParser.resolveReference(devfile, fileContentProvider);
      devfile = ensureToUseGenerateName(devfile);

      return newDto(FactoryDto.class)
          .withV(CURRENT_VERSION)
          .withDevfile(DtoConverter.asDto(devfile))
          .withSource(location.filename().isPresent() ? location.filename().get() : null);

    } else if (devfileVersion.devfileMajorVersion(devfileJson) == 2) {
      return newDto(FactoryDevfileV2Dto.class)
          .withV(CURRENT_VERSION)
          .withDevfile(devfileParser.convertYamlToMap(devfileJson))
          .withSource(location.filename().isPresent() ? location.filename().get() : null);
    } else {
      throw new DevfileException("Unknown devfile version.");
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
    attributes.put(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, defaultChePlugins);

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
