package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import javax.inject.Inject;
import javax.inject.Named;

public class GatewayHostExternalServiceExposureStrategy extends
    SingleHostExternalServiceExposureStrategy implements ExternalServiceExposureStrategy {

  public static final String GATEWAY_HOST_STRATEGY = "gateway-host";

  @Inject
  public GatewayHostExternalServiceExposureStrategy(@Named("che.host") String cheHost) {
    super(cheHost);
  }
}
