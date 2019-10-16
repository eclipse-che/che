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

package org.eclipse.che.api.workspace.server;

import static org.eclipse.che.api.core.model.workspace.config.Command.PREVIEW_URL_ATTRIBUTE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

/**
 * Helps to generate links for Preview URLs and update commands with these links.
 * <p>
 * For preview URLs, we need to include following data in {@link org.eclipse.che.api.workspace.shared.dto.WorkspaceDto}:
 * <li>Map with final preview url links where keys are composed from prefix {@link
 * PreviewUrlLinksVariableGenerator#PREVIEW_URL_VARIABLE_PREFIX}, modified command name and CRC
 * hash.</li>
 * <li>Each command that has defined Preview url must have attribute `previewUrl` referencing to
 * correct key in the map in format that IDE will understand.</li>
 */
@Singleton
class PreviewUrlLinksVariableGenerator {

  private static final String PREVIEW_URL_VARIABLE_PREFIX = "previewurl/";

  /**
   * Takes commands from given {@code workspace}. For all commands that have defined previewUrl,
   * creates variable name in defined format and final preview url link. It updates the command so
   * it's `previewUrl` attribute will contain variable in proper format. Method then returns map of
   * all preview url links with these variables as keys:
   * <pre>
   *   links:
   *     "previewURl/run_123": http://your.domain/some/path
   *   command:
   *     attributes:
   *       previewUrl: '${previewUrl/run_123}'
   * </pre>
   *
   * @return map of all <commandPreviewUrlVariable, previewUrlFullLink>
   */
  Map<String, String> genLinksMapAndUpdateCommands(WorkspaceImpl workspace, UriBuilder uriBuilder) {
    if (workspace.getRuntime() != null && workspace.getRuntime().getCommands() != null) {

      Map<String, String> links = new HashMap<>();
      for (Command command : workspace.getRuntime().getCommands()) {
        Map<String, String> commandAttributes = command.getAttributes();

        if (command.getPreviewUrl() != null
            && commandAttributes.containsKey(PREVIEW_URL_ATTRIBUTE)) {
          String previewUrlKey = createPreviewUrlLinkKeyForCommand(command);

          String previewUrlFinalUri =
              uriBuilder
                  .clone()
                  .host(commandAttributes.remove(PREVIEW_URL_ATTRIBUTE))
                  .replacePath(command.getPreviewUrl().getPath())
                  .build()
                  .toString();

          links.put(previewUrlKey, previewUrlFinalUri);

          commandAttributes.put(PREVIEW_URL_ATTRIBUTE, formatVariable(previewUrlKey));
        }
      }
      return links;
    }
    return Collections.emptyMap();
  }

  /**
   * Creates link key for given command in format `previewUrl/<commandName_withoutSpaces>_<crc(command.name)>`
   */
  private String createPreviewUrlLinkKeyForCommand(Command command) {
    return PREVIEW_URL_VARIABLE_PREFIX
        + command.getName().replaceAll(" ", "")
        + "_"
        + calculateCrcFromCommandName(command.getName());
  }

  private String calculateCrcFromCommandName(String commandName) {
    CRC32 crc32 = new CRC32();
    for (byte b : commandName.getBytes()) {
      crc32.update(b);
    }
    return "" + crc32.getValue();
  }

  private String formatVariable(String var) {
    return "${" + var + "}";
  }
}
