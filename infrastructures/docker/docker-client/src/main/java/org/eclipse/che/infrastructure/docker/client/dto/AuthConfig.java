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
package org.eclipse.che.infrastructure.docker.client.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Implementation of docker AuthConfig object
 *
 * @author andrew00x
 * @see <a href="https://github.com/docker/docker/blob/v1.6.0/registry/auth.go#L29">source</a>
 */
@DTO
public interface AuthConfig {
  String getUsername();

  void setUsername(String username);

  AuthConfig withUsername(String username);

  String getPassword();

  void setPassword(String password);

  AuthConfig withPassword(String password);
}
