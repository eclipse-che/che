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
import static java.util.Collections.emptyList;

import com.google.common.annotations.Beta;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
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
      String[] plugins = splitAttribute(pluginsAttribute);
      if (plugins.length != 0) {
        metaFQNs.addAll(parsePluginFQNs(plugins));
      }
    }
    if (!isNullOrEmpty(editorAttribute)) {
      String[] editor = splitAttribute(editorAttribute);
      if (editor.length > 1) {
        throw new InfrastructureException(
            "Multiple editors found in workspace config attributes. "
                + "Only one editor is supported per workspace.");
      }
      metaFQNs.addAll(parsePluginFQNs(editor));
    }
    return metaFQNs;
  }

  private Collection<PluginFQN> parsePluginFQNs(String... plugins) throws InfrastructureException {
    List<PluginFQN> collectedFQNs = new ArrayList<>();
    for (String plugin : plugins) {
      URI repo = null;
      String idVersionString;
      final int idVersionTagDelimiter = plugin.lastIndexOf("/");
      idVersionString = plugin.substring(idVersionTagDelimiter + 1);
      if (idVersionTagDelimiter > -1) {
        try {
          repo = new URI(plugin.substring(0, idVersionTagDelimiter));
        } catch (URISyntaxException e) {
          throw new InfrastructureException(
              String.format(
                  "Plugin registry URL is incorrect. Problematic plugin entry: %s", plugin));
        }
      }
      String[] idAndVersion = idVersionString.split(":");
      if (idAndVersion.length != 2 || idAndVersion[0].isEmpty() || idAndVersion[1].isEmpty()) {
        throw new InfrastructureException(
            String.format("Plugin format is illegal. Problematic plugin entry: %s", plugin));
      }
      if (collectedFQNs
          .stream()
          .anyMatch(
              p -> p.getId().equals(idAndVersion[0]) && p.getVersion().equals(idAndVersion[1]))) {
        throw new InfrastructureException(
            String.format(
                "Invalid Che tooling plugins configuration: plugin %s:%s is duplicated",
                idAndVersion[0], idAndVersion[1])); // even if different repos
      }
      collectedFQNs.add(new PluginFQN(repo, idAndVersion[0], idAndVersion[1]));
    }
    return collectedFQNs;
  }

  private String[] splitAttribute(String attribute) {
    String[] plugins = attribute.split(",");
    return Arrays.stream(plugins).map(s -> s.trim()).toArray(String[]::new);
  }
}
