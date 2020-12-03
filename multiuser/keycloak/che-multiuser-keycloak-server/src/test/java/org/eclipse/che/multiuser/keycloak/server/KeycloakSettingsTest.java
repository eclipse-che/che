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
package org.eclipse.che.multiuser.keycloak.server;

import org.testng.annotations.Test;

/** @author Ilya Buziuk */
public class KeycloakSettingsTest {

  @Test(expectedExceptions = RuntimeException.class)
  public void shouldNotThrowNPE() {
    new KeycloakSettings(null, null, null, null, null, null, null, null, false, null, null, false);
  }
}
