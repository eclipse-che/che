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
package org.eclipse.che.selenium.core.requestfactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;

/** @author Anton Korneta */
@Singleton
public class CheTestDefaultHttpJsonRequestFactory extends TestUserHttpJsonRequestFactory {

  @Inject
  public CheTestDefaultHttpJsonRequestFactory(
      TestAuthServiceClient authServiceClient, DefaultTestUser defaultTestUser) {
    super(authServiceClient, defaultTestUser);
  }
}
