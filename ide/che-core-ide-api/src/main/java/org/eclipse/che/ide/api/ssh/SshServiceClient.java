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
package org.eclipse.che.ide.api.ssh;

import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;

/**
 * The client service for working with ssh keys.
 *
 * @author Sergii Leschenko
 */
public interface SshServiceClient {
  /** Gets ssh pairs of given service */
  Promise<List<SshPairDto>> getPairs(String service);

  /**
   * Gets ssh pair of given service and specific name
   *
   * @param service the service name
   * @param name the identifier of one the pair
   */
  Promise<SshPairDto> getPair(String service, String name);

  /** Generates new ssh key pair with given service and name */
  Promise<SshPairDto> generatePair(String service, String name);

  /** Deletes ssh pair with given service and name */
  Promise<Void> deletePair(String service, String name);
}
