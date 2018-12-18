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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider of "cors.allowed.origins" setting for CORS Filter of WS Agent. Provides the value of WS
 * Master domain, by inferring it from "che.api" property
 */
public class CheWsAgentCorsAllowedOriginsProvider implements Provider<String> {

  private static final Logger LOG =
      LoggerFactory.getLogger(CheWsAgentCorsAllowedOriginsProvider.class);

  private final String allowedOrigins;

  @Inject
  public CheWsAgentCorsAllowedOriginsProvider(
      @Named("che.api") String cheApi,
      @Nullable @Named("che.wsagent.cors.allowed_origins") String allowedOrigins) {
    if (allowedOrigins == null) {
      this.allowedOrigins = UriBuilder.fromUri(cheApi).replacePath(null).build().toString();
      LOG.info("auto provision of URL, allowed origins is {}", allowedOrigins);
    } else {
      this.allowedOrigins = allowedOrigins;
      LOG.info("URL provided from property", allowedOrigins);
    }
  }

  @Override
  public String get() {
    return allowedOrigins;
  }
}
