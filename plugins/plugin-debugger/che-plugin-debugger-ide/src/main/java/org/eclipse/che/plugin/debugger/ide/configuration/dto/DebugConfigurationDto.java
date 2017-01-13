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
package org.eclipse.che.plugin.debugger.ide.configuration.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/** @author Artem Zatsarynnyi */
@DTO
public interface DebugConfigurationDto {

    String getName();

    void setName(String name);

    DebugConfigurationDto withName(String name);

    String getType();

    void setType(String type);

    DebugConfigurationDto withType(String type);

    String getHost();

    void setHost(String host);

    DebugConfigurationDto withHost(String host);

    int getPort();

    void setPort(int port);

    DebugConfigurationDto withPort(int port);

    Map<String, String> getConnectionProperties();

    void setConnectionProperties(Map<String, String> connectionProperties);

    DebugConfigurationDto withConnectionProperties(Map<String, String> connectionProperties);
}
