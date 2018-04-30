/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import javax.inject.Singleton;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;

import io.fabric8.kubernetes.client.Config;

/**
 * @author David Festal
 */
@Singleton
public class ConfigBuilder {

  /**
   * Builds the Openshift {@link Config} object based on a default {@link Config} object and an
   * optional workspace Id.
   */
  protected Config buildConfig(Config defaultConfig, @Nullable String workspaceId)
      throws InfrastructureException {
    return defaultConfig;
  }
}
