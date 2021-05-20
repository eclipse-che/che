/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static java.util.Objects.requireNonNull;

/** Lists all the possible types of workspace exposure. */
public enum WorkspaceExposureType {
  GATEWAY("gateway"),
  NATIVE("native");

  private final String configValue;

  WorkspaceExposureType(String configValue) {
    this.configValue = configValue;
  }

  public String getConfigValue() {
    return configValue;
  }

  public static WorkspaceExposureType fromConfigurationValue(String configValue) {
    requireNonNull(configValue);
    for (WorkspaceExposureType s : WorkspaceExposureType.values()) {
      if (s.configValue.equals(configValue)) {
        return s;
      }
    }

    throw new IllegalArgumentException("Unknown server resolver strategy: " + configValue);
  }
}
