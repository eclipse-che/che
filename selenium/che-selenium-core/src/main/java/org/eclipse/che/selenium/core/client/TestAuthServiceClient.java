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
package org.eclipse.che.selenium.core.client;

/** @author Anatolii Bazko */
public interface TestAuthServiceClient {

  /** Logs user into the system and returns auth token. */
  String login(String username, String password) throws Exception;

  void logout(String token) throws Exception;
}
