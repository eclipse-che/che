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
package org.eclipse.che.selenium.core.requestfactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;

/** @author Anton Korneta */
public class CheTestDefaultUserHttpJsonRequestFactory extends TestUserHttpJsonRequestFactory {

  @Inject
  public CheTestDefaultUserHttpJsonRequestFactory(
      TestAuthServiceClient authServiceClient,
      @Named("che.testuser.email") String email,
      @Named("che.testuser.password") String password) {
    super(authServiceClient, email, password);
  }
}
