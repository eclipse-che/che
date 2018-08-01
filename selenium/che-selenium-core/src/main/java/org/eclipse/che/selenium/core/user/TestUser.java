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
package org.eclipse.che.selenium.core.user;

import java.io.IOException;

/**
 * Represents a user in a test environment.
 *
 * @author Anatolii Bazko
 */
public interface TestUser {
  /** Returns user's email. */
  String getEmail();

  /** Returns user's password. */
  String getPassword();

  /** Re-login to product to obtain authentication token of the user. */
  String obtainAuthToken();

  /** Return user's offline token. */
  String getOfflineToken();

  /** Return user's name. */
  String getName();

  /** Return user's id. */
  String getId();

  /** Remove user. */
  void delete() throws IOException;
}
