/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import io.fabric8.kubernetes.client.Config;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth.IdentityProviderConfigFactory;

/**
 * This class allows customizing the OpenShift {@link Config} returned by the {@link
 * OpenShiftClientFactory} according to the current context (workspace ID, current user).
 *
 * @author David Festal
 * @see IdentityProviderConfigFactory
 */
public class OpenShiftClientConfigFactory {

  /**
   * Builds the Openshift {@link Config} object based on a default {@link Config} object and an
   * optional workspace Id.
   */
  public Config buildConfig(Config defaultConfig, @Nullable String workspaceId)
      throws InfrastructureException {
    return defaultConfig;
  }

  /**
   * Returns true if implementation personalizes config to the current subject, otherwise returns
   * false if default config is always used.
   */
  public boolean isPersonalized() {
    return false;
  }
}
