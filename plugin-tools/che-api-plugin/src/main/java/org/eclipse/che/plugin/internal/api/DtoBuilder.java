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
package org.eclipse.che.plugin.internal.api;

import org.eclipse.che.plugin.dto.PluginCheReloaded;
import org.eclipse.che.plugin.dto.PluginDescriptor;
import org.eclipse.che.plugin.dto.PluginInstall;

import java.util.List;

/**
 * Builder used to create DTO objects.
 * @author Florent Benoit
 */
public interface DtoBuilder {

    /**
     * Convert an existing internal object to its DTO value.
     * @param plugin the internal plugin
     * @return user friendly DTO object
     */
    PluginDescriptor convert(IPlugin plugin);

    /**
     * Convert list of internal plugin object to its DTO value.
     * @param plugins the list of internal plugins
     * @return user friendly DTO object
     */

    List<PluginDescriptor> convert(List<IPlugin> plugins);

    PluginInstall convert(IPluginInstall pluginInstall);

    List<PluginInstall> convertInstall(List<IPluginInstall> iPluginInstalls);


    PluginCheReloaded cheReloaded();
}
