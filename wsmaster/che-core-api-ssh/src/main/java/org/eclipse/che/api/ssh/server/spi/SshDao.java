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
package org.eclipse.che.api.ssh.server.spi;

import java.util.List;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;

/**
 * Defines data access object contract for {@link SshPairImpl}.
 *
 * @author Sergii Leschenko
 */
public interface SshDao {

  /**
   * Creates new ssh pair for specified user.
   *
   * @param sshPair ssh pair to create
   * @throws ConflictException when specified user already has ssh pair with given service and name
   * @throws NullPointerException when {@code sshPair} is null
   * @throws ServerException when any other error occurs during ssh pair creating
   */
  void create(SshPairImpl sshPair) throws ServerException, ConflictException;

  /**
   * Returns ssh pairs by owner and service.
   *
   * @param owner the id of the user who is the owner of the ssh pairs
   * @param service service name of ssh pair
   * @return list of ssh pair with given service and owned by given service.
   * @throws NullPointerException when {@code owner} or {@code service} is null
   * @throws ServerException when any other error occurs during ssh pair fetching
   */
  List<SshPairImpl> get(String owner, String service) throws ServerException;

  /**
   * Returns ssh pair by owner, service and name.
   *
   * @param owner the id of the user who is the owner of the ssh pair
   * @param service service name of ssh pair
   * @param name name of ssh pair
   * @return ssh pair instance
   * @throws NullPointerException when {@code owner} or {@code service} or {@code name} is null
   * @throws NotFoundException when ssh pair is not found
   * @throws ServerException when any other error occurs during ssh pair fetching
   */
  SshPairImpl get(String owner, String service, String name)
      throws ServerException, NotFoundException;

  /**
   * Removes ssh pair by owner, service and name.
   *
   * @param owner the id of the user who is the owner of the ssh pair
   * @param service service name of ssh pair
   * @param name of ssh pair
   * @throws NullPointerException when {@code owner} or {@code service} or {@code name} is null
   * @throws NotFoundException when ssh pair is not found
   * @throws ServerException when any other error occurs during ssh pair removing
   */
  void remove(String owner, String service, String name) throws ServerException, NotFoundException;

  /**
   * Gets ssh pairs by owner.
   *
   * @param owner the owner of the ssh key
   * @return the list of the ssh key pairs owned by the {@code owner}, or empty list if there are no
   *     ssh key pairs by the given {@code owner}
   * @throws NullPointerException when {@code owner} is null
   * @throws ServerException when any error occurs(e.g. database connection error)
   */
  List<SshPairImpl> get(String owner) throws ServerException;
}
