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
import java.util.List;

/**
 * Defines repository of plugins
 * @author Florent Benoit
 */
public interface PluginRepository {

    /**
     * @return list of available plugins
     * @throws PluginRepositoryException
     */
    List<Path> getAvailablePlugins() throws PluginRepositoryException;

    /**
     * @return list of plugins that are staged
     * @throws PluginRepositoryException
     */
    List<Path> getStagedInstallPlugins() throws PluginRepositoryException;

    /**
     * @return list of plugins that are staged
     * @throws PluginRepositoryException
     */
    List<Path> getStagedUninstallPlugins() throws PluginRepositoryException;

    /**
     * @return list of plugins that are installed
     * @throws PluginRepositoryException
     */
    List<Path> getInstalledPlugins() throws PluginRepositoryException;

    /**
     * Add a plugin and make it available
     * @throws PluginRepositoryException if can't add
     */
    Path add(Path localPlugin) throws PluginRepositoryException;

    /**
     * Plugin is going to stage directory to be installed
     * @param availablePlugin plugin that may be installed
     */
    Path stageInstall(Path availablePlugin) throws PluginRepositoryException;

    /**
     * Cancel plugin is going to stage directory to be installed
     * @param stagedInstallPlugin plugin that may be installed
     */
    Path undoStageInstall(Path stagedInstallPlugin) throws PluginRepositoryException;

    /**
     * Ask to uninstall the plugin
     * @param installedPlugin an existing installed plugin
     */
    Path stageUninstall(Path installedPlugin) throws PluginRepositoryException;

    /**
     * Cancel plugin is going to stage directory to be uninstalled
     * @param stagedUninstallPlugin plugin that may be uninstalled
     */
    Path undoStageUninstall(Path stagedUninstallPlugin) throws PluginRepositoryException;

    /**
     * Called before staging operation
     * @throws PluginRepositoryException
     */
    void preStaged() throws PluginRepositoryException;

    /**
     * Callback to call when stage operation has been successfully done.
     * It will move all staged plugin to the installed part or uninstall parts to available
     * @throws PluginRepositoryException
     */
    void stagedComplete() throws PluginRepositoryException;


    /**
     * Callback to call when stage operation has failed. It will cancel all current operations
     * @throws PluginRepositoryException
     */
    void stagedFailed() throws PluginRepositoryException;

    /**
     * Remove an existing plugin from the available folder
     * @param availablePlugin path to the plugin
     * @throws PluginRepositoryException
     */
    void remove(Path availablePlugin) throws PluginRepositoryException;
}
