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

import java.nio.file.Path;

/**
 * Defines the internal API of a plugin like having path to this plugin, name, status, etc.
 * @author Florent Benoit
 */
public interface IPlugin {

    /**
     * @return Path of the plugin
     */
    Path getPath();

    /**
     * @return name of the plugin
     */
    String getName();

    /**
     * @return status {@link IPluginStatus} of the given plugin
     */
    IPluginStatus getStatus();

    /**
     * @return category {@link IPluginCategory} of the plugin
     */
    IPluginCategory getCategory();

    /**
     * @return version of the plugin
     */
    String getVersion();

}
