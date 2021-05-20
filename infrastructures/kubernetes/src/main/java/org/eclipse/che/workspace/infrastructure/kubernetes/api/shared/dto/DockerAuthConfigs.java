/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * Implementation of docker model ConfigFile object
 *
 * @author Max Shaposhnik
 * @see <a href="https://github.com/docker/docker/blob/v1.6.0/registry/auth.go#L37">source</a>
 */
@DTO
public interface DockerAuthConfigs {

  Map<String, DockerAuthConfig> getConfigs();

  void setConfigs(Map<String, DockerAuthConfig> configs);

  DockerAuthConfigs withConfigs(Map<String, DockerAuthConfig> configs);
}
