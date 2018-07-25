/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
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
  String login(String username, String password, String offlineToken) throws Exception;

  void logout(String token) throws Exception;
}
