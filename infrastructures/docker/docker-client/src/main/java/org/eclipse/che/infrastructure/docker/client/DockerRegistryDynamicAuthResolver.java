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
package org.eclipse.che.infrastructure.docker.client;

import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.infrastructure.docker.client.dto.AuthConfig;

/**
 * Resolves dynamic auth config for docker registries.
 *
 * @author Mykola Morhun
 */
public interface DockerRegistryDynamicAuthResolver {
  /**
   * Retrieves actual auth data for given registry. If no credential found for given registry null
   * will be returned.
   *
   * @param registry registry to which
   * @return dynamic auth data for specified registry or null if no credential found
   */
  @Nullable
  AuthConfig getXRegistryAuth(@Nullable String registry);

  /**
   * Retrieves all actual auth configs for all configured registries with dynamic auth credentials.
   * If no registries with dynamic auth credentials found, empty map will be returned.
   *
   * @return all dynamic auth configs or empty map if no credentials found
   */
  Map<String, AuthConfig> getXRegistryConfig();
}
