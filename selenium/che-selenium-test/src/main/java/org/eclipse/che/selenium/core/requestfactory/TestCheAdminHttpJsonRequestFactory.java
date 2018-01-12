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

import com.google.inject.name.Named;
import javax.inject.Inject;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;

/** @author Dmytro Nochevnov */
public class TestCheAdminHttpJsonRequestFactory extends TestUserHttpJsonRequestFactory {

  @Inject
  public TestCheAdminHttpJsonRequestFactory(
      TestAuthServiceClient authServiceClient,
      @Named("che.admin.email") String adminEmail,
      @Named("che.admin.password") String adminPassword) {
    super(authServiceClient, adminEmail, adminPassword);
  }
}
