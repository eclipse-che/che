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

import java.util.List;

/**
 * @author Florent Benoit
 */
public interface PluginManager {

    /**
     * @return list of all plugins managed by the system
     */
    List<IPlugin> listPlugins() throws PluginRepositoryException;

    IPlugin addPlugin(String mavenPluginRef) throws PluginManagerException, PluginResolverNotFoundException;


    IPlugin getPluginDetails(String pluginName) throws PluginManagerNotFoundException;
    IPlugin removePlugin(String pluginName) throws PluginManagerException;
    IPlugin updatePlugin(String pluginName, IPluginAction pluginAction) throws PluginManagerException, PluginRepositoryException;


    IPluginInstall requireNewInstall() throws PluginManagerException, PluginInstallerException;
    IPluginInstall getInstall(long id) throws PluginInstallerNotFoundException;

    /**
     * Gets the list of all install
     * @return the IDs of every install
     */
    List<IPluginInstall> listInstall() throws PluginManagerException;


}
