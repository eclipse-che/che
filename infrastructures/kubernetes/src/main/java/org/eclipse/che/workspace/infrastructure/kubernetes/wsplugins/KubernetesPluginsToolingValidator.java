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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static java.lang.String.format;

import java.util.List;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginBase;

@Singleton
public class KubernetesPluginsToolingValidator {

  private static final Pattern namePattern = Pattern.compile("");

  public void validatePluginNames(List<? extends PluginBase> plugins)
      throws InternalInfrastructureException {
    for (PluginBase plugin : plugins) {
      final String formattedPluginName = plugin.getName().toLowerCase();
      if (!namePattern.matcher(formattedPluginName).matches()) {
        throw new InternalInfrastructureException(
            format(
                "Plugin named `%s` contains unacceptable symbols and cannot be used as part of container naming.",
                formattedPluginName));
      }
    }
  }
}
