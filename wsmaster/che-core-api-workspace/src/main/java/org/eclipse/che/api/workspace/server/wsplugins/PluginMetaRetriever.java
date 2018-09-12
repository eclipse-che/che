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
package org.eclipse.che.api.workspace.server.wsplugins;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches Che tooling plugin objects corresponding to attributes of a workspace config.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksander Garagatyi
 */
@Beta
public class PluginMetaRetriever {

  private static final Logger LOG = LoggerFactory.getLogger(PluginMetaRetriever.class);
  private static final String CHE_PLUGIN_OBJECT_ERROR =
      "Che plugin '%s:%s' configuration is invalid. %s";
  private static final String PLUGIN_REGISTRY_PROPERTY = "che.workspace.plugin_registry_url";

  private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());

  private final UriBuilder pluginRegistry;

  @Inject
  public PluginMetaRetriever(@Nullable @Named(PLUGIN_REGISTRY_PROPERTY) String pluginRegistry) {
    if (pluginRegistry == null) {
      LOG.info(
          format(
              "Che tooling plugins feature is disabled - Che plugin registry API endpoint property '%s' is not configured",
              PLUGIN_REGISTRY_PROPERTY));
      this.pluginRegistry = null;
    } else {
      this.pluginRegistry = UriBuilder.fromUri(pluginRegistry).path("plugins");
    }
  }

  /**
   * Gets Che tooling plugins list from provided workspace config attributes, fetches corresponding
   * meta objects from Che plugin registry and returns list of {@link PluginMeta} with meta
   * information about plugins in a workspace.
   *
   * <p>This API is in <b>Beta</b> and is subject to changes or removal.
   *
   * @param attributes workspace config attributes
   * @throws InfrastructureException when attributes contain invalid Che plugins entries or Che
   *     plugin meta files retrieval from Che plugin registry fails or returns invalid data
   */
  @Beta
  public Collection<PluginMeta> get(Map<String, String> attributes) throws InfrastructureException {
    if (pluginRegistry == null || attributes == null || attributes.isEmpty()) {
      return emptyList();
    }
    String pluginsAttribute = attributes.get(Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE);
    String editorAttribute = attributes.get(Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE);

    ArrayList<Pair<String, String>> metasIdsVersions = new ArrayList<>();
    if (!isNullOrEmpty(pluginsAttribute)) {
      String[] plugins = pluginsAttribute.split(" *, *");
      if (plugins.length != 0) {
        Collection<Pair<String, String>> pluginsIdsVersions = parseIdsVersions(plugins);
        metasIdsVersions.addAll(pluginsIdsVersions);
      }
    }
    if (!isNullOrEmpty(editorAttribute)) {
      Collection<Pair<String, String>> editorIdVersionCollection =
          parseIdsVersions(editorAttribute);
      if (editorIdVersionCollection.size() > 1) {
        throw new InfrastructureException(
            "Multiple editors found in workspace config attributes. "
                + "It is not supported. Please, use one editor only.");
      }
      metasIdsVersions.addAll(editorIdVersionCollection);
    }

    return getMetas(metasIdsVersions);
  }

  private Collection<Pair<String, String>> parseIdsVersions(String... idsVersions)
      throws InfrastructureException {
    Map<String, Pair<String, String>> collectedIdVersion = new HashMap<>();
    for (String plugin : idsVersions) {
      String[] idVersion = plugin.split(":");
      if (idVersion.length != 2 || idVersion[0].isEmpty() || idVersion[1].isEmpty()) {
        throw new InfrastructureException(
            "Plugin format is illegal. Problematic plugin entry:" + plugin);
      }
      String key = idVersion[0] + ':' + idVersion[1];
      if (collectedIdVersion.containsKey(key)) {
        throw new InfrastructureException(
            format("Invalid Che tooling plugins configuration: plugin %s is duplicated", key));
      }
      collectedIdVersion.put(key, Pair.of(idVersion[0], idVersion[1]));
    }
    return collectedIdVersion.values();
  }

  private Collection<PluginMeta> getMetas(ArrayList<Pair<String, String>> metasIdsVersions)
      throws InfrastructureException {
    ArrayList<PluginMeta> metas = new ArrayList<>();
    for (Pair<String, String> metaIdVersion : metasIdsVersions) {
      metas.add(getMeta(metaIdVersion.first, metaIdVersion.second));
    }

    return metas;
  }

  private PluginMeta getMeta(String id, String version) throws InfrastructureException {
    try {
      URI metaURI = pluginRegistry.clone().path(id).path(version).path("meta.yaml").build();

      PluginMeta meta = getBody(metaURI, PluginMeta.class);
      validateMeta(meta, id, version);
      return meta;
    } catch (IllegalArgumentException | UriBuilderException e) {
      throw new InternalInfrastructureException(
          format("Metadata of plugin %s:%s retrieval failed", id, version));
    } catch (IOException e) {
      throw new InfrastructureException(
          format(
              "Error occurred on retrieval of plugin %s. Error: %s",
              id + ':' + version, e.getMessage()));
    }
  }

  private void validateMeta(PluginMeta meta, String id, String version)
      throws InfrastructureException {
    requireNotNullNorEmpty(meta.getId(), CHE_PLUGIN_OBJECT_ERROR, id, version, "ID is missing.");
    requireEqual(
        id,
        meta.getId(),
        "Plugin id in attribute doesn't match plugin metadata. Plugin object seems broken.");
    requireNotNullNorEmpty(
        meta.getVersion(), CHE_PLUGIN_OBJECT_ERROR, id, version, "Version is missing.");
    requireEqual(
        version,
        meta.getVersion(),
        "Plugin version in workspace config attributes doesn't match plugin metadata. Plugin object seems broken.");
    requireNotNullNorEmpty(
        meta.getName(), CHE_PLUGIN_OBJECT_ERROR, id, version, "Name is missing.");
    requireNotNullNorEmpty(
        meta.getType(), CHE_PLUGIN_OBJECT_ERROR, id, version, "Type is missing.");
    requireNotNullNorEmpty(meta.getUrl(), CHE_PLUGIN_OBJECT_ERROR, id, version, "URL is missing.");
  }

  @VisibleForTesting
  protected <T> T getBody(URI uri, Class<T> clas) throws IOException {
    HttpURLConnection httpURLConnection = null;
    try {
      httpURLConnection = (HttpURLConnection) uri.toURL().openConnection();

      int responseCode = httpURLConnection.getResponseCode();
      if (responseCode != 200) {
        throw new IOException(
            format(
                "Can't get object by URI '%s'. Error: %s",
                uri.toString(), getError(httpURLConnection)));
      }

      return parseYamlResponseStreamAndClose(httpURLConnection.getInputStream(), clas);
    } finally {
      if (httpURLConnection != null) {
        httpURLConnection.disconnect();
      }
    }
  }

  private String getError(HttpURLConnection httpURLConnection) throws IOException {
    try (InputStreamReader isr = new InputStreamReader(httpURLConnection.getInputStream())) {
      return CharStreams.toString(isr);
    }
  }

  protected <T> T parseYamlResponseStreamAndClose(InputStream inputStream, Class<T> clazz)
      throws IOException {
    try (InputStreamReader reader = new InputStreamReader(inputStream)) {
      return YAML_PARSER.readValue(reader, clazz);
    } catch (IOException e) {
      throw new IOException(
          "Internal server error. Unexpected response body received from Che plugin registry API."
              + e.getLocalizedMessage(),
          e);
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void requireNotNullNorEmpty(String s, String error, String... errorArgs)
      throws InfrastructureException {
    if (s == null || s.isEmpty()) {
      throw new InfrastructureException(format(error, (Object[]) errorArgs));
    }
  }

  private void requireEqual(String version, String version1, String error, String... errorArgs)
      throws InfrastructureException {
    if (!version.equals(version1)) {
      throw new InfrastructureException(format(error, (Object[]) errorArgs));
    }
  }
}
