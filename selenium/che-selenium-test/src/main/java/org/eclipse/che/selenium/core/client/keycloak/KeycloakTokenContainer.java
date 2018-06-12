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
package org.eclipse.che.selenium.core.client.keycloak;

import com.google.gson.annotations.SerializedName;

/** @author Dmytro Nochevnov */
public class KeycloakTokenContainer {
  @SerializedName("token")
  private KeycloakToken token;

  public KeycloakTokenContainer() {}

  public KeycloakToken getToken() {
    return token;
  }
}
