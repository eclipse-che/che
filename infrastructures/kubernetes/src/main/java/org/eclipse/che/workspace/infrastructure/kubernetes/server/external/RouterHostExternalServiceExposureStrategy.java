package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import javax.inject.Inject;
import javax.inject.Named;

public class RouterHostExternalServiceExposureStrategy extends
    SingleHostExternalServiceExposureStrategy implements ExternalServiceExposureStrategy {

  public static final String ROUTER_HOST_STRATEGY = "router-host";

  @Inject
  public RouterHostExternalServiceExposureStrategy(@Named("che.host") String cheHost) {
    super(cheHost);
  }
}
