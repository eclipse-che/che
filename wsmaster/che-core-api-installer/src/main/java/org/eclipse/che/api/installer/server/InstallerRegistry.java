/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server;

import java.util.List;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.installer.server.exception.IllegalInstallerKeyException;
import org.eclipse.che.api.installer.server.exception.InstallerAlreadyExistsException;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.server.impl.InstallerFqn;
import org.eclipse.che.api.installer.shared.model.Installer;

/**
 * The registry for installers that might be injected into machine.
 *
 * @author Anatoliy Bazko
 * @author Sergii Leshchenko
 * @see Installer
 */
public interface InstallerRegistry {

  /**
   * Adds installer to the registry.
   *
   * @param installer the installer to add
   * @throws InstallerAlreadyExistsException if installer with corresponding {@link InstallerFqn}
   *     already exists
   * @throws InstallerException if unexpected error occurred
   */
  void add(Installer installer) throws InstallerException;

  /**
   * Updates installer in the registry.
   *
   * @param installer the installer to update
   * @throws InstallerNotFoundException if installer with corresponding {@link InstallerFqn} does
   *     not exist in the registry
   * @throws InstallerException if unexpected error occurred
   */
  void update(Installer installer) throws InstallerException;

  /**
   * Removes installer from the registry.
   *
   * @param installerKey the installer key
   * @throws IllegalInstallerKeyException if specified installer key has wrong format
   * @throws InstallerException if unexpected error occurred
   */
  void remove(String installerKey) throws InstallerException;

  /**
   * Gets {@link Installer} by key.
   *
   * @param installerKey the installer key
   * @return {@link Installer}
   * @throws IllegalInstallerKeyException if specified installer key has wrong format
   * @throws InstallerNotFoundException if installer not found in the registry
   * @throws InstallerException if unexpected error occurred
   */
  Installer getInstaller(String installerKey) throws InstallerException;

  /**
   * Returns a list of the available versions of the specific installer.
   *
   * @param id the id of the installer
   * @return list of versions
   * @throws InstallerException if unexpected error occurred
   */
  List<String> getVersions(String id) throws InstallerException;

  /**
   * Returns all installers using pagination.
   *
   * @param maxItems the maximum number of installers to return
   * @param skipCount the number of installers to skip
   * @return list of installers or empty list if no installers were found
   * @throws IllegalArgumentException when {@code maxItems} or {@code skipCount} is negative
   * @throws InstallerException if unexpected error occurred
   */
  Page<? extends Installer> getInstallers(int maxItems, int skipCount) throws InstallerException;

  /**
   * Traverses dependencies of all listed installers and returns properly ordered list of
   * non-duplicated installer descriptions. If any of {@code installerKeys} contains only id then
   * the latest version of this installer will be used to fetch dependencies.
   *
   * @param installerKeys installers keys to fetch dependencies and order
   * @return list of installers
   * @throws IllegalInstallerKeyException if specified installer key has wrong format
   * @throws InstallerNotFoundException if some of specified installer is not found in the registry
   * @throws InstallerException if unexpected error occurred
   */
  List<Installer> getOrderedInstallers(List<String> installerKeys) throws InstallerException;
}
