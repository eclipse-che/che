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
package org.eclipse.che.plugin;

import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.plugin.internal.api.PluginConfiguration;

import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration used to setup all paths and variables for Che Plugin rest service
 * @author Florent Benoit
 */
@Singleton
public class ChePluginConfiguration implements PluginConfiguration {

    /**
     * Install script is not the same between unix and windows
     * @return
     */
    @Override
    public Path getInstallScript() {
        if (SystemInfo.isWindows()) {
            return getCheHome().resolve("extInstall.bat");
        }
        return getCheHome().resolve("extInstall.sh");
    }

    /**
     * @return path to the plugins folder
     */
    @Override
    public Path getPluginRootFolder() {
        return new File(System.getProperty("catalina.base"), "plugins").toPath();
    }

    /**
     * @return path to the extensions folder
     */
    @Override
    public Path getExtRootFolder() {
        return new File(System.getProperty("catalina.base"), "ext").toPath();
    }

    /**
     * @return path to the Eclipse Che home folder
     */
    @Override
    public Path getCheHome() {
        return new File(System.getProperty("catalina.base")).toPath();
    }

    /**
     * @return local path to maven repository
     */
    @Override
    public Path getLocalMavenRepository() {
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path m2Repository = userHome.resolve(".m2/repository");
        return m2Repository;
    }

    /**
     * Path to the templates folder inside Eclipse Che
     *
     * @return the path to the templates
     */
    @Override
    public Path getTemplatesRootFolder() {
        return getCheHome().resolve("che-templates");
    }

    /**
     * Path to the machines folder inside Eclipse Che
     *
     * @return the path to the machines
     */
    @Override
    public Path getMachinesRootFolder() {
        return getCheHome().resolve("recipes");
    }
}
