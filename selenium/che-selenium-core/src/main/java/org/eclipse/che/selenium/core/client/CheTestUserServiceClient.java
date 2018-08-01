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
package org.eclipse.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactory;

/** @author Anton Korneta */
@Singleton
public class CheTestUserServiceClient extends TestUserServiceClientImpl {

  @Inject
  public CheTestUserServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestUserHttpJsonRequestFactory requestFactory) {
    super(apiEndpointProvider, requestFactory);
  }

  @Override
  public void create(String name, String email, String password)
      throws BadRequestException, ConflictException, ServerException {}

  @Override
  public void remove(String id) throws ServerException, ConflictException {}
}
