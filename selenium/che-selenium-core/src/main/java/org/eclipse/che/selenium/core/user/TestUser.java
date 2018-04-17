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
