/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import com.google.inject.Singleton;

/** @author Anatolii Bazko */
@Singleton
public class CheTestAuthServiceClient implements TestAuthServiceClient {

  @Override
  public String login(String username, String password) throws Exception {
    return username;
  }

  @Override
  public void logout(String token) throws Exception {}
}
