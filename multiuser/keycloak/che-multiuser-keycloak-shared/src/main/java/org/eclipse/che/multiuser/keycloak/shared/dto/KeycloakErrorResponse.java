package org.eclipse.che.multiuser.keycloak.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@DTO
public interface KeycloakErrorResponse {

  String getErrorMessage();

  void setErrorMessage(String errorMessage);

  KeycloakErrorResponse withErrorMessage(String errorMessage);

}
