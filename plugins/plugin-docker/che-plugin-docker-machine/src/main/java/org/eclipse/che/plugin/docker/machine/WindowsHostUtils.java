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
package org.eclipse.che.plugin.docker.machine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provide methods for working with Docker on Windows Host
 * according to limitations docker+windows.
 *
 * @author Vitalii Parfonov
 */
public class WindowsHostUtils {

    /**
     * Path to the Che configuration folder on Windows in user home.
     * According to some limitations of docker+Windows all mounted to the docker container folder must be in user.home
     * directory.
     */
    public static final String WINDOWS_CHE_HOME_PATH = "\\AppData\\Local\\Eclipse Che";

    /**
     * Create Che Home directory if not exist on Windows hosts it will be locate in
     * System.getProperty("user.home") + {@link WindowsHostUtils#WINDOWS_CHE_HOME_PATH}
     * @return Path to Che home dir
     * @throws IOException
     */
    public static Path ensureCheHomeExist() throws IOException {
        Path cheHome = Paths.get(System.getProperty("user.home") + WINDOWS_CHE_HOME_PATH);
        if (Files.notExists(cheHome)) {
            Files.createDirectories(cheHome);
        }
        return cheHome;
    }
}
