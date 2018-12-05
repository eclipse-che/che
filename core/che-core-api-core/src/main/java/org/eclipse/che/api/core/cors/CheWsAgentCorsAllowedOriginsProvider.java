package org.eclipse.che.api.core.cors;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.inject.Named;

/**
 * Provider of "cors.allowed.origins" setting for CORS Filter of WS Agent. Provides the value
 * of WS Master domain, by inferring it from "che.api" property
 */
public class CheWsAgentCorsAllowedOriginsProvider implements Provider<String> {

  private String allowedOrigins;

  @Inject
  public CheWsAgentCorsAllowedOriginsProvider(@Named("che.api") String cheApi) {
    this.allowedOrigins = cheApi.substring(0, cheApi.length() - 4);
  }

  @Override
  public String get() {
    return allowedOrigins;
  }
}
