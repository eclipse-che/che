/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.shared.dto;

import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.model.machine.ServerProperties;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes how to access to exposed ports for servers inside machine
 *
 * @author Alexander Garagatyi
 */
@DTO
public interface ServerDto extends Server {
    @Override
    String getProtocol();

    void setProtocol(String protocol);

    ServerDto withProtocol(String protocol);

    @Override
    String getAddress();

    void setAddress(String address);

    ServerDto withAddress(String address);

    @Override
    String getUrl();

    void setUrl(String url);

    ServerDto withUrl(String url);

    @Override
    String getRef();

    void setRef(String ref);

    ServerDto withRef(String ref);

    @Override
    ServerPropertiesDto getProperties();

    void setProperties(ServerPropertiesDto properties);

    ServerDto withProperties(ServerPropertiesDto properties);
}
