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

import com.google.common.annotations.Beta;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  private static final String INCORRECT_PLUGIN_FORMAT_TEMPLATE =
      "Plugin '%s' has incorrect format. Should be: 'registryURL/publisher/name/version' or 'publisher/name/version'";
  private static final String REGISTRY_PATTERN = "https?://[-./\\w]+(:[0-9]+)?(/[-./\\w]+)?";
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
      Pattern.compile("((?<registry>" + REGISTRY_PATTERN + ")/)?(?<id>" + ID_PATTERN + ")");

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

      if (collectedFQNs.stream().anyMatch(p -> p.getId().equals(pFQN.getId()))) {
        throw new InfrastructureException(
            format(
                "Invalid Che tooling plugins configuration: plugin %s is duplicated",
                pFQN.getId())); // even if different registries
      }
      collectedFQNs.add(pFQN);
    }
    return collectedFQNs;
  }

  public ExtendedPluginFQN parsePluginFQN(String plugin) throws InfrastructureException {
    String registry;
    String id;
    String publisher;
    String name;
    String version;
    URI registryURI = null;
    Matcher matcher = PLUGIN_PATTERN.matcher(plugin);
    if (matcher.matches()) {
      registry = matcher.group("registry");
      id = matcher.group("id");
      publisher = matcher.group("publisher");
      name = matcher.group("name");
      version = matcher.group("version");
    } else {
      throw new InfrastructureException(format(INCORRECT_PLUGIN_FORMAT_TEMPLATE, plugin));
    }
    if (!isNullOrEmpty(registry)) {
      try {
        registryURI = new URI(registry);
      } catch (URISyntaxException e) {
        throw new InfrastructureException(
            format(
                "Plugin registry URL '%s' is invalid. Problematic plugin entry: '%s'",
                registry, plugin));
      }
    }

    return new ExtendedPluginFQN(registryURI, id, publisher, name, version);
  }

  private String[] splitAttribute(String attribute) {
    String[] plugins = attribute.split(",");
    return Arrays.stream(plugins).map(String::trim).toArray(String[]::new);
  }
}
