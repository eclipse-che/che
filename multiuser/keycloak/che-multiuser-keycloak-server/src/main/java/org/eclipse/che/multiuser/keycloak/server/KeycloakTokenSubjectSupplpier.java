package org.eclipse.che.multiuser.keycloak.server;

import static com.google.common.base.Strings.isNullOrEmpty;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import java.io.IOException;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.authentication.commons.SubjectSupplier;
import org.eclipse.che.multiuser.api.permission.server.AuthorizedSubject;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeycloakTokenSubjectSupplpier implements SubjectSupplier {

  private static final Logger LOG =
      LoggerFactory.getLogger(KeycloakTokenSubjectSupplpier.class);

  private final JwtParser jwtParser;
  private final KeycloakUserManager userManager;
  private final PermissionChecker permissionChecker;
  private final KeycloakProfileRetriever keycloakProfileRetriever;
  private final KeycloakSettings keycloakSettings;

  public KeycloakTokenSubjectSupplpier(JwtParser jwtParser,
      KeycloakUserManager userManager,
      PermissionChecker permissionChecker,
      KeycloakProfileRetriever keycloakProfileRetriever,
      KeycloakSettings keycloakSettings) {
    this.jwtParser = jwtParser;
    this.userManager = userManager;
    this.permissionChecker = permissionChecker;
    this.keycloakProfileRetriever = keycloakProfileRetriever;
    this.keycloakSettings = keycloakSettings;
  }

  @Override
  public Subject getSubject(ServletRequest request, ServletResponse response, FilterChain chain,
      String token) throws IOException, ServletException {
    Claims claims;
    try {
      Jws<Claims> jwt = jwtParser.parseClaimsJws(token);
      claims = jwt.getBody();
      LOG.debug("JWT = {}", jwt);
      // OK, we can trust this JWT
    } catch (ExpiredJwtException e) {
      sendError(response, 401, "The specified token is expired");
      return null;
    } catch (JwtException e) {
      sendError(response, 401, "Token validation failed: " + e.getMessage());
      return null;
    }

    try {
      String username =
          claims.get(
              keycloakSettings.get().get(KeycloakConstants.USERNAME_CLAIM_SETTING),
              String.class);
      if (username == null) { // fallback to unique id promised by spec
        // https://openid.net/specs/openid-connect-basic-1_0.html#ClaimStability
        username = claims.getIssuer() + ":" + claims.getSubject();
      }
      String id = claims.getSubject();

      String email = retrieveEmail(token, claims, username, id);
      if (email == null) {
        sendError(
            response,
            400,
            "Unable to authenticate user because email address is not set in keycloak profile");
        return null;
      }
      User user = userManager.getOrCreateUser(id, email, username);
      return
          new AuthorizedSubject(
              new SubjectImpl(user.getName(), user.getId(), token, false), permissionChecker);
    } catch (ServerException | ConflictException e) {
      throw new ServletException(
          "Unable to identify user " + claims.getSubject() + " in Che database", e);
    }
  }

  private String retrieveEmail(String token, Claims claims,
      String username, String id) throws ServerException, IOException {
    String email = claims.get("email", String.class);

    if (isNullOrEmpty(email)) {
      boolean userNotFound = false;
      try {
        userManager.getById(id);
      } catch (NotFoundException e) {
        userNotFound = true;
      }
      if (userNotFound) {
        try {
          EnvironmentContext.getCurrent()
              .setSubject(new SubjectImpl(username, id, token, true));
          Map<String, String> profileAttributes =
              keycloakProfileRetriever.retrieveKeycloakAttributes();
          email = profileAttributes.get("email");
        } finally {
          EnvironmentContext.reset();
        }
      }
    }
    return email;
  }

  private void sendError(ServletResponse res, int errorCode, String message) throws IOException {
    HttpServletResponse response = (HttpServletResponse) res;
    response.getOutputStream().write(message.getBytes());
    response.setStatus(errorCode);
  }


}
