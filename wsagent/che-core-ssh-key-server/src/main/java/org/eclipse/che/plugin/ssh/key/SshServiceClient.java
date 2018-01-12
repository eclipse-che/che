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
package org.eclipse.che.plugin.ssh.key;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;

/**
 * Client for working with ssh service
 *
 * @author Sergii Leschenko
 */
public interface SshServiceClient {
  /**
   * Generates and store ssh pair for current user.
   *
   * @param request config for generating ssh pair
   * @return instance of generated ssh pair
   * @throws ServerException when some error occurred while generating or saving ssh pair
   */
  SshPairDto generatePair(GenerateSshPairRequest request) throws ServerException;

  /**
   * Creates ssh pair for current user.
   *
   * @param sshPair ssh pair to create
   * @throws ServerException when some error occurred while creating ssh pair
   */
  void createPair(SshPairDto sshPair) throws ServerException;

  /**
   * Gets ssh pair by service and name.
   *
   * @param service service name of ssh pair
   * @param name name of ssh pair
   * @return instance of ssh pair
   * @throws NotFoundException when ssh pair is not found
   * @throws ServerException when any other error occurs during ssh pair fetching
   */
  SshPairDto getPair(String service, String name) throws ServerException, NotFoundException;

  /**
   * Removes ssh pair by service and name of current user.
   *
   * @param service service name of ssh pair
   * @param name name of ssh pair
   * @throws NotFoundException when ssh pair is not found
   * @throws ServerException when any other error occurs during ssh pair removing
   */
  void removePair(String service, String name) throws ServerException, NotFoundException;
}
