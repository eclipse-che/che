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
package org.eclipse.che.api.core.cors;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.inject.Named;

/**
 * Provider of "cors.support.credentials" setting for CORS Filter of WS Agent. Provides the value of
 * WS Master domain, by inferring it from "che.api" property
 */
public class CheWsAgentCorsAllowCredentialsProvider implements Provider<Boolean> {

  private final boolean allowCredentials;

  @Inject
  public CheWsAgentCorsAllowCredentialsProvider(
      @Named("che.cors.allow_credentials") boolean allowedOrigins) {
    this.allowCredentials = true;
  }

  @Override
  public boolean get() {
    return allowCredentials;
  }
}
