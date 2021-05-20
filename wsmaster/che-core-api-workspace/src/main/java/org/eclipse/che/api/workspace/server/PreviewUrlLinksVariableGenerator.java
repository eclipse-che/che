/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static org.eclipse.che.api.core.model.workspace.config.Command.PREVIEW_URL_ATTRIBUTE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

/**
 * Helps to generate links for Preview URLs and update commands with these links.
 *
 * <p>For preview URLs, we need to include following data in {@link
 * org.eclipse.che.api.workspace.shared.dto.WorkspaceDto}:
 * <li>Map with final preview url links where keys are composed from prefix {@link
 *     PreviewUrlLinksVariableGenerator#PREVIEW_URL_VARIABLE_PREFIX}, modified command name and CRC
 *     hash.
 * <li>Each command that has defined Preview url must have attribute `previewUrl` referencing to
 *     correct key in the map in format that IDE will understand.
 */
@Singleton
class PreviewUrlLinksVariableGenerator {

  private static final String PREVIEW_URL_VARIABLE_PREFIX = "previewurl/";

  /**
   * Takes commands from given {@code workspace}. For all commands that have defined previewUrl,
   * creates variable name in defined format and final preview url link. It updates the command so
   * it's `previewUrl` attribute will contain variable in proper format. Method then returns map of
   * all preview url links with these variables as keys:
   *
   * <pre>
   *   links:
   *     "previewURl/run_123": http://your.domain
   *   command:
   *     attributes:
   *       previewUrl: '${previewUrl/run_123}/some/path'
   * </pre>
   *
   * @return map of all <commandPreviewUrlVariable, previewUrlFullLink>
   */
  Map<String, String> genLinksMapAndUpdateCommands(WorkspaceImpl workspace, UriBuilder uriBuilder) {
    if (workspace == null
        || workspace.getRuntime() == null
        || workspace.getRuntime().getCommands() == null
        || uriBuilder == null) {
      return Collections.emptyMap();
    }

    Map<String, String> links = new HashMap<>();
    for (Command command : workspace.getRuntime().getCommands()) {
      Map<String, String> commandAttributes = command.getAttributes();

      if (command.getPreviewUrl() != null
          && commandAttributes != null
          && commandAttributes.containsKey(PREVIEW_URL_ATTRIBUTE)) {
        String previewUrlLinkValue = createPreviewUrlLinkValue(uriBuilder, command);
        String previewUrlLinkKey = createPreviewUrlLinkKey(command);
        links.put(previewUrlLinkKey, previewUrlLinkValue);

        commandAttributes.replace(
            PREVIEW_URL_ATTRIBUTE,
            formatAttributeValue(previewUrlLinkKey, command.getPreviewUrl().getPath()));
      }
    }
    return links;
  }

  private String createPreviewUrlLinkValue(UriBuilder uriBuilder, Command command) {
    UriBuilder previewUriBuilder =
        uriBuilder.clone().host(command.getAttributes().get(PREVIEW_URL_ATTRIBUTE));
    previewUriBuilder.replacePath(null);
    return previewUriBuilder.build().toString();
  }

  /**
   * Creates link key for given command in format
   * `previewUrl/<commandName_withoutSpaces>_<hash(command.name)>`
   */
  private String createPreviewUrlLinkKey(Command command) {
    return PREVIEW_URL_VARIABLE_PREFIX
        + command.getName().replaceAll(" ", "")
        + "_"
        + Math.abs(command.getName().hashCode());
  }

  private String formatAttributeValue(String var, String path) {
    String previewUrlAttributeValue = "${" + var + "}";
    if (path != null) {
      previewUrlAttributeValue += path;
    }
    return previewUrlAttributeValue;
  }
}
