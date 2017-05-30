/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

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
