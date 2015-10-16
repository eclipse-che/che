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
 * Defines descriptor of a plugin which is the unit managed by all plugins operations.
 * @author Florent Benoit
 */
@DTO
public interface PluginDescriptor {

    // name of plugin
    String getName();
    void setName(String name);
    PluginDescriptor withName(String name);

    // version of plugin
    String getVersion();
    void setVersion(String version);
    PluginDescriptor withVersion(String version);

    // status of the plugin
    PluginStatus getStatus();
    void setStatus(PluginStatus status);
    PluginDescriptor withStatus(PluginStatus status);

    // category of the plugin (USER | SYSTEM)
    PluginCategory getCategory();
    void setCategory(PluginCategory category);
    PluginDescriptor withCategory(PluginCategory category);


}
