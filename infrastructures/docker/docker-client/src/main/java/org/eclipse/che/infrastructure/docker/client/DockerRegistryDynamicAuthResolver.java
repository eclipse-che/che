/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client;

import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfig;

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
