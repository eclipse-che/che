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
package org.eclipse.che.api.git.shared;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Contains additional info about the credential provider, such as provider name, URL ans some other
 * that can be used to authorize in order to perform operations.
 *
 * @author Max Shaposhnik
 */
public class ProviderInfo {

  public static final String AUTHENTICATE_URL = "authenticateUrl";

  public static final String PROVIDER_NAME = "providerName";

  private Map<String, String> info = new HashMap<>();

  public ProviderInfo(@NotNull String providerName) {
    info.put(PROVIDER_NAME, providerName);
  }

  public ProviderInfo(@NotNull String providerName, @NotNull String authenticateUrl) {
    info.put(PROVIDER_NAME, providerName);
    info.put(AUTHENTICATE_URL, authenticateUrl);
  }

  public String getProviderName() {
    return info.get(PROVIDER_NAME);
  }

  /** @return authenticate URL. It retrun String or null value. */
  @Nullable
  public String getAuthenticateUrl() {
    return info.get(AUTHENTICATE_URL);
  }

  public void put(String key, String value) {
    info.put(key, value);
  }

  public String get(String key) {
    return info.get(key);
  }
}
