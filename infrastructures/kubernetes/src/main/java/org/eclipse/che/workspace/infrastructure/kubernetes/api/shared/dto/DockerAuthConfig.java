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
package org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Implementation of docker AuthConfig object
 *
 * @author andrew00x
 * @see <a href="https://github.com/docker/docker/blob/v1.6.0/registry/auth.go#L29">source</a>
 */
@DTO
public interface DockerAuthConfig {
  String getUsername();

  void setUsername(String username);

  DockerAuthConfig withUsername(String username);

  String getPassword();

  void setPassword(String password);

  DockerAuthConfig withPassword(String password);
}
