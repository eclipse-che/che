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
package org.eclipse.che.infrastructure.docker.auth.dto;

import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * Implementation of docker model ConfigFile object
 *
 * @author Max Shaposhnik
 * @see <a href="https://github.com/docker/docker/blob/v1.6.0/registry/auth.go#L37">source</a>
 */
@DTO
public interface AuthConfigs {

  Map<String, AuthConfig> getConfigs();

  void setConfigs(Map<String, AuthConfig> configs);

  AuthConfigs withConfigs(Map<String, AuthConfig> configs);
}
