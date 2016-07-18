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
     * Creates Che home folder if not exist on Windows host. Che home folder is located in
     * System.getProperty("user.home") + {@link WindowsHostUtils#WINDOWS_CHE_HOME_PATH}
     *
     * @return Path to Che home dir
     * @throws IOException if implicit folders creation failed
     */
    public static Path ensureCheHomeExist() throws IOException {
        return ensureCheHomeExist(true);
    }

    /**
     * Creates Che home folder if not exist on Windows host. Che home folder is located in
     * System.getProperty("user.home") + {@link WindowsHostUtils#WINDOWS_CHE_HOME_PATH}
     *
     * @param allowFoldersCreation
     *         whether creation of needed directories is allowed
     * @return Path to Che home dir
     * @throws IOException
     *         if folders creation failed or needed folder is missing (depends on value of parameter of the method)
     */
    public static Path ensureCheHomeExist(boolean allowFoldersCreation) throws IOException {
        Path cheHome = Paths.get(System.getProperty("user.home") + WINDOWS_CHE_HOME_PATH);
        if (Files.notExists(cheHome)) {
            if (allowFoldersCreation) {
                Files.createDirectories(cheHome);
            } else {
                throw new IOException(String.format("Folder %s is missing and its creation is disallowed", cheHome));
            }
        }
        return cheHome;
    }

    /**
     * Returns Che home directory on Windows hosts
     *
     * @return path to Che home dir
     */
    public static Path getCheHome() {
        return Paths.get(System.getProperty("user.home") + WINDOWS_CHE_HOME_PATH);
    }
}
