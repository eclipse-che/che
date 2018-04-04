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
package org.eclipse.che.multiuser.keycloak.token.provider.validator;

import org.eclipse.che.multiuser.keycloak.token.provider.exception.KeycloakException;
import org.junit.BeforeClass;
import org.junit.Test;

public class KeycloakTokenValidatorTest {
  private static final String VALID_TOKEN = "Bearer token";
  private static final String INVALID_TOKEN = "token";
  private static KeycloakTokenValidator keycloakTokenValidator;

  @BeforeClass
  public static void init() {
    keycloakTokenValidator = new KeycloakTokenValidator();
  }

  @Test
  public void processValidToken() throws KeycloakException {
    keycloakTokenValidator.validate(VALID_TOKEN);
  }

  @Test(expected = KeycloakException.class)
  public void processInvalidToken() throws KeycloakException {
    keycloakTokenValidator.validate(INVALID_TOKEN);
  }
}
