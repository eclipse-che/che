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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static java.lang.String.format;

import java.util.List;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;

@Singleton
public class KubernetesPluginsToolingValidator {

  // Pattern is from K8S Container class
  private static final Pattern namePattern = Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?");

  public void validatePluginNames(List<? extends ChePlugin> plugins) throws ValidationException {
    for (ChePlugin plugin : plugins) {
      if (plugin.getName() != null) {
        final String formattedPluginName = plugin.getName().toLowerCase();
        checkValid(
            formattedPluginName,
            "Plugin name `%s` contains unacceptable symbols and cannot be used as part of container naming.");
      }
      for (CheContainer container : plugin.getContainers()) {
        if (container.getName() != null) {
          final String formattedContainerName = container.getName().toLowerCase();
          checkValid(
              formattedContainerName,
              "Container name `%s` contains unacceptable symbols and cannot be used as part of container naming.");
        }
      }
    }
  }

  private void checkValid(String input, String errorMessage) throws ValidationException {
    if (!namePattern.matcher(input).matches()) {
      throw new ValidationException(format(errorMessage, input));
    }
  }
}
