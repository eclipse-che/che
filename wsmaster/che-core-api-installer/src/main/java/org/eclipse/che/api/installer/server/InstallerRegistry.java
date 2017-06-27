/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.installer.server;

import org.eclipse.che.api.installer.server.exception.IllegalInstallerKey;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.shared.model.Installer;

import java.util.Collection;
import java.util.List;

/**
 * The registry for installers that might be injected into machine.
 *
 * @author Anatoliy Bazko
 * @author Sergii Leshchenko
 * @see Installer
 */
public interface InstallerRegistry {

    /**
     * Gets {@link Installer} by key.
     *
     * @param installerKey
     *         the installer key
     * @return {@link Installer}
     * @throws IllegalInstallerKey
     *         if specified installer key has wrong format
     * @throws InstallerNotFoundException
     *         if installer not found in the registry
     * @throws InstallerException
     *         if unexpected error occurred
     */
    Installer getInstaller(String installerKey) throws InstallerException;

    /**
     * Returns a list of the available versions of the specific installer.
     *
     * @param id
     *         the id of the installer
     * @return list of versions
     * @throws InstallerNotFoundException
     *         if installer not found in the registry
     * @throws InstallerException
     *         if unexpected error occurred
     */
    List<String> getVersions(String id) throws InstallerException;


    /**
     * Returns the collection of available installers.
     *
     * @return collection of installers
     * @throws InstallerException
     *         if unexpected error occurred
     */
    Collection<Installer> getInstallers() throws InstallerException;

    /**
     * Traverses dependencies of all listed installers and
     * returns properly ordered list of non-duplicated installer descriptions
     *
     * @param installers
     *         installers to fetch dependencies and order
     * @return list of installers
     * @throws IllegalInstallerKey
     *         if specified installer key has wrong format
     * @throws InstallerNotFoundException
     *         if some of specified installer is not found in the registry
     * @throws InstallerException
     *         if unexpected error occurred
     */
    List<Installer> getOrderedInstallers(List<String> installers) throws InstallerException;
}
