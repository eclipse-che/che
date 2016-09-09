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

import org.eclipse.che.api.core.model.machine.ServerProperties;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes how to access servers properties
 *
 * @author Mario Loriedo
 */
@DTO
public interface ServerPropertiesDto extends ServerProperties {
    @Override
    String getPath();

    void setPath(String path);

    ServerPropertiesDto withPath(String path);

    @Override
    String getInternalAddress();

    void setInternalAddress(String internalAddress);

    ServerPropertiesDto withInternalAddress(String internalAddress);

    @Override
    String getInternalUrl();

    void setInternalUrl(String internalUrl);

    ServerPropertiesDto withInternalUrl(String internalUrl);
}
