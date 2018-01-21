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
