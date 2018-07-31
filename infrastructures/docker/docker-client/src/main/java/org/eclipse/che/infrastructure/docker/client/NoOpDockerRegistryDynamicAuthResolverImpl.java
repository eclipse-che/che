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

import java.util.Collections;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfig;

/** @author Mykola Morhun */
public class NoOpDockerRegistryDynamicAuthResolverImpl
    implements DockerRegistryDynamicAuthResolver {
  @Override
  @Nullable
  public AuthConfig getXRegistryAuth(@Nullable String registry) {
    return null;
  }

  @Override
  public Map<String, AuthConfig> getXRegistryConfig() {
    return Collections.emptyMap();
  }
}
