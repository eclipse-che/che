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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.Beta;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ExtendedPluginFQN;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.api.workspace.shared.Constants;

/**
 * Parses workspace attributes into a list of {@link PluginFQN}.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksander Garagatyi
 * @author Angel Misevski
 */
@Beta
public class PluginFQNParser {

  private final FileContentProvider fileContentProvider;
  private ObjectMapper yamlReader;

  @Inject
  public PluginFQNParser(FileContentProvider fileContentProvider) {
    this.fileContentProvider = fileContentProvider;
    this.yamlReader = new ObjectMapper(new YAMLFactory());
  }

  private static final String INCORRECT_PLUGIN_FORMAT_TEMPLATE =
      "Plugin '%s' has incorrect format. Should be: 'registryURL#publisher/name/version' or 'publisher/name/version' or `referenceURL`";
  private static final String URL_PATTERN = "https?://[-.\\w]+(:[0-9]+)?(/[-.\\w]+)*";
  private static final String PUBLISHER_PATTERN = "[-a-z0-9]+";
  private static final String NAME_PATTERN = "[-a-z0-9]+";
  private static final String VERSION_PATTERN = "[-.a-z0-9]+";
  private static final String ID_PATTERN =
      "(?<publisher>"
          + PUBLISHER_PATTERN
          + ")/(?<name>"
          + NAME_PATTERN
          + ")/(?<version>"
          + VERSION_PATTERN
          + ")";
  private static final Pattern PLUGIN_PATTERN =
      Pattern.compile("((?<url>" + URL_PATTERN + ")(/?)(#?))?(?<id>" + ID_PATTERN + ")?");

  /**
   * Parses a workspace attributes map into a collection of {@link PluginFQN}.
   *
   * @param attributes workspace attributes containing plugin and/or editor fields
   * @return a Collection of PluginFQN containing the editor and all plugins for this attributes
   * @throws InfrastructureException if attributes defines more than one editor
   */
  public Collection<PluginFQN> parsePlugins(Map<String, String> attributes)
      throws InfrastructureException {
    if (attributes == null) {
      return emptyList();
    }

    String pluginsAttribute =
        attributes.getOrDefault(Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, null);
    String editorAttribute =
        attributes.getOrDefault(Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, null);

    List<PluginFQN> metaFQNs = new ArrayList<>();
    if (!isNullOrEmpty(pluginsAttribute)) {
      metaFQNs.addAll(parsePluginFQNs(pluginsAttribute));
    }
    if (!isNullOrEmpty(editorAttribute)) {
      Collection<PluginFQN> editorsFQNs = parsePluginFQNs(editorAttribute);
      if (editorsFQNs.size() > 1) {
        throw new InfrastructureException(
            "Multiple editors found in workspace config attributes. "
                + "Only one editor is supported per workspace.");
      }
      metaFQNs.addAll(editorsFQNs);
    }
    return metaFQNs;
  }

  private Collection<PluginFQN> parsePluginFQNs(String attribute) throws InfrastructureException {

    String[] plugins = splitAttribute(attribute);
    if (plugins.length == 0) {
      return emptyList();
    }

    List<PluginFQN> collectedFQNs = new ArrayList<>();
    for (String plugin : plugins) {
      PluginFQN pFQN = parsePluginFQN(plugin);

      String pluginKey = firstNonNull(pFQN.getReference(), pFQN.getId());
      if (collectedFQNs
          .stream()
          .anyMatch(p -> pluginKey.equals(p.getId()) || pluginKey.equals(p.getReference()))) {
        throw new InfrastructureException(
            format(
                "Invalid Che tooling plugins configuration: plugin %s is duplicated",
                pluginKey)); // even if different registries
      }
      collectedFQNs.add(pFQN);
    }
    return collectedFQNs;
  }

  public ExtendedPluginFQN parsePluginFQN(String plugin) throws InfrastructureException {
    String url;
    String id;
    String publisher;
    String name;
    String version;
    URI registryURI = null;
    Matcher matcher = PLUGIN_PATTERN.matcher(plugin);
    if (matcher.matches()) {
      url = matcher.group("url");
      id = matcher.group("id");
      publisher = matcher.group("publisher");
      name = matcher.group("name");
      version = matcher.group("version");
    } else {
      throw new InfrastructureException(format(INCORRECT_PLUGIN_FORMAT_TEMPLATE, plugin));
    }
    if (!isNullOrEmpty(url)) {
      if (isNullOrEmpty(id)) {
        // reference only
        return new ExtendedPluginFQN(url);
      } else {
        // registry + id
        try {
          registryURI = new URI(url);
        } catch (URISyntaxException e) {
          throw new InfrastructureException(
              format(
                  "Plugin registry URL '%s' is invalid. Problematic plugin entry: '%s'",
                  url, plugin));
        }
      }
    }
    return new ExtendedPluginFQN(registryURI, id, publisher, name, version);
  }

  private String[] splitAttribute(String attribute) {
    String[] plugins = attribute.split(",");
    return Arrays.stream(plugins).map(String::trim).toArray(String[]::new);
  }

  public ExtendedPluginFQN evaluateFqn(String reference) throws InfrastructureException {
    try {
      String pluginMetaContent = fileContentProvider.fetchContent(reference);
      JsonNode contentNode = yamlReader.readTree(pluginMetaContent);
      String publisher = contentNode.get("publisher").textValue();
      String name = contentNode.get("name").textValue();
      String version = contentNode.get("version").textValue();
      return new ExtendedPluginFQN(
          null,
          new StringJoiner("/").add(publisher).add(name).add(version).toString(),
          publisher,
          name,
          version);
    } catch (DevfileException | IOException e) {
      throw new InfrastructureException(format("Plugin reference URL '%s' is invalid.", reference));
    }
  }
}
