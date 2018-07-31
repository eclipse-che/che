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
package org.eclipse.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.CheTestAdminHttpJsonRequestFactory;

/** This util is handling the requests to Organization API as admin. */
@Singleton
public class CheTestAdminOrganizationServiceClient extends TestOrganizationServiceClient {

  @Inject
  public CheTestAdminOrganizationServiceClient(
      TestApiEndpointUrlProvider apiEndpointUrlProvider,
      CheTestAdminHttpJsonRequestFactory adminRequestFactory) {
    super(apiEndpointUrlProvider, adminRequestFactory);
  }
}
