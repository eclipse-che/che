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
package org.eclipse.che.selenium.core.requestfactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;

/** @author Anton Korneta */
public class CheTestDefaultUserHttpJsonRequestFactory extends TestUserHttpJsonRequestFactory {

  @Inject
  public CheTestDefaultUserHttpJsonRequestFactory(
      TestAuthServiceClient authServiceClient,
      @Named("che.testuser.name") String name,
      @Named("che.testuser.password") String password,
      @Named("che.testuser.offline_token") String offlineToken) {
    super(authServiceClient, name, password, offlineToken);
  }
}
