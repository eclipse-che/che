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

import javax.inject.Inject;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.user.AdminTestUser;

/** @author Dmytro Nochevnov */
public class CheTestAdminHttpJsonRequestFactory extends TestUserHttpJsonRequestFactory {

  @Inject
  public CheTestAdminHttpJsonRequestFactory(
      TestAuthServiceClient authServiceClient, AdminTestUser adminTestUser) {
    super(authServiceClient, adminTestUser);
  }
}
