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
package org.eclipse.che.api.system.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for system service events.
 *
 * @author Yevhenii Voevodin
 */
@DTO
public interface SystemServiceEventDto extends SystemEventDto {

    /**
     * Returns the name of the service described by this event.
     */
    String getService();

    void setService(String service);

    SystemServiceEventDto withService(String service);
}
