/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server.spi;

import java.util.List;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.installer.server.exception.InstallerAlreadyExistsException;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.server.impl.InstallerFqn;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;

/**
 * Defines data access object contract for {@link InstallerImpl}.
 *
 * <p>The implementation is not required to be responsible for persistent layer data dto integrity.
 * It simply transfers data from one layer to another, so if you're going to call any of implemented
 * methods it is considered that all needed verifications are already done.
 *
 * <p><strong>Note:</strong> This particularly does not mean that method call will not make any
 * inconsistency, but this mean that such kind of inconsistencies are expected by design and may be
 * treated further.
 *
 * @author Anatolii Bazko
 */
public interface InstallerDao {

  /**
   * Creates a new installer.
   *
   * @param installer installer to create
   * @throws NullPointerException when {@code installer} is null
   * @throws InstallerAlreadyExistsException when installer with such id and version already exists
   * @throws InstallerException when any other error occurs
   */
  void create(InstallerImpl installer) throws InstallerException;

  /**
   * Updates installer by replacing an existing entity with a new one.
   *
   * @param installer installer to update
   * @throws NullPointerException when {@code installer} is null
   * @throws InstallerNotFoundException when installer with id and version doesn't exist
   * @throws InstallerException when any other error occurs
   */
  void update(InstallerImpl installer) throws InstallerException;

  /**
   * Removes installer.
   *
   * <p>Note that this method doesn't throw any exception when installer doesn't exist.
   *
   * @param fqn unique installer identifier
   * @throws NullPointerException when {@code fqn} is null
   * @throws InstallerException when any other error occurs
   */
  void remove(InstallerFqn fqn) throws InstallerException;

  /**
   * Finds installer by its fqn.
   *
   * @param fqn unique installer identifier
   * @return installer instance, never null
   * @throws NullPointerException when {@code fqn} is null
   * @throws InstallerNotFoundException when installer with given {@code fqn} doesn't exist
   * @throws InstallerException when any other error occurs
   */
  InstallerImpl getByFqn(InstallerFqn fqn) throws InstallerException;

  /**
   * Gets all available versions of the installer with giving id.
   *
   * @param id the installer id {@link InstallerFqn#id}
   * @return list of installers or empty list if no installers were found
   * @throws InstallerException when any other error occurs
   */
  List<String> getVersions(String id) throws InstallerException;

  /**
   * Gets all installers from persistent layer.
   *
   * @param maxItems the maximum number of installers to return
   * @param skipCount the number of installers to skip
   * @return list of installers or empty list if no installers were found
   * @throws IllegalArgumentException when {@code maxItems} or {@code skipCount} is negative
   * @throws InstallerException when any other error occurs
   */
  Page<InstallerImpl> getAll(int maxItems, long skipCount) throws InstallerException;

  /**
   * Get count of all installers from persistent layer.
   *
   * @return installer count
   * @throws InstallerException when any error occurs
   */
  long getTotalCount() throws InstallerException;
}
