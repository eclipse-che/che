/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Descriptor when Eclipse Che WebApp is reloaded
 * @author Florent Benoit
 */
@DTO
public interface PluginCheReloaded {

    /**
     * @return Status of the operation
     */
    PluginCheStatus getStatus();

    /**
     * Defines the status
     * @param status Status of the operation
     */
    void setStatus(PluginCheStatus status);


    /**
     * Defines the status
     * @param status Status of the operation
     * @return current instance
     */
    PluginCheReloaded withStatus(PluginCheStatus status);
}
