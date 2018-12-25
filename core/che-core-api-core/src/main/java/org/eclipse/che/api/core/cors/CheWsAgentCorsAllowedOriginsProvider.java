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
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Provider of "cors.allowed.origins" setting for CORS Filter of WS Agent. Provides the value of WS
 * Master domain, by inferring it from "che.api" property
 */
public class CheWsAgentCorsAllowedOriginsProvider implements Provider<String> {

  private final String allowedOrigins;

  @Inject
  public CheWsAgentCorsAllowedOriginsProvider(
      @Named("che.api.external") String cheApi,
      @Nullable @Named("che.wsagent.cors.allowed_origins") String allowedOrigins) {
    if (allowedOrigins == null) {
      this.allowedOrigins = UriBuilder.fromUri(cheApi).replacePath(null).build().toString();
    } else {
      this.allowedOrigins = allowedOrigins;
    }
  }

  @Override
  public String get() {
    return allowedOrigins;
  }
}
