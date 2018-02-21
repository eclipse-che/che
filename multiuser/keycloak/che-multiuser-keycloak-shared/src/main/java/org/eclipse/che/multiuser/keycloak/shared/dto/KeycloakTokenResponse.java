package org.eclipse.che.multiuser.keycloak.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@DTO
public interface KeycloakTokenResponse {

   String getAccess_token();

   void setAccess_token(String accessToken);

   KeycloakTokenResponse withAccess_token(String accessToken);

   String getTokenType();

   void setTokenType(String tokenType);

   KeycloakTokenResponse withTokenType(String tokenType);

   String getScope();

   void setScope(String scope);

   KeycloakTokenResponse withScope(String scope);

}
