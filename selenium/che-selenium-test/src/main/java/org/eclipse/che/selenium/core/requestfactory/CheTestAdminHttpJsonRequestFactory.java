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
