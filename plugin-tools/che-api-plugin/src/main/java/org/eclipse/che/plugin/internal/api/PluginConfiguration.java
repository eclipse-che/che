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
 * Defines configuration for the plugin
 * @author Florent Benoit
 */
public interface PluginConfiguration {

    Path getInstallScript();

    Path getPluginRootFolder();

    Path getExtRootFolder();

    Path getCheHome();

    Path getLocalMavenRepository();

    /**
     * Path to the templates folder inside Eclipse Che
     * @return the path to the templates
     */
    Path getTemplatesRootFolder();

    /**
     * Path to the machines folder inside Eclipse Che
     * @return the path to the machines
     */
    Path getMachinesRootFolder();
}
