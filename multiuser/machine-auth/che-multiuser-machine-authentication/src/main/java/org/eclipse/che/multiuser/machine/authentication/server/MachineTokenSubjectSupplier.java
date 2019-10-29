package org.eclipse.che.multiuser.machine.authentication.server;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_ID_CLAIM;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.WORKSPACE_ID_CLAIM;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.authentication.commons.SubjectSupplier;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;

public class MachineTokenSubjectSupplier implements SubjectSupplier {

  private JwtParser jwtParser;
  private UserManager userManager;
  private PermissionChecker permissionChecker;

  public MachineTokenSubjectSupplier(JwtParser jwtParser,
      UserManager userManager,
      PermissionChecker permissionChecker) {
    this.jwtParser = jwtParser;
    this.userManager = userManager;
    this.permissionChecker = permissionChecker;
  }

  @Override
    public Subject getSubject(ServletRequest request, ServletResponse response, FilterChain chain, String token) throws IOException, ServletException {
      try {
        return extractSubject(token);
      } catch (NotMachineTokenJwtException ex) {
        // not a machine token, bypass
        chain.doFilter(request, response);
      } catch (NotFoundException e) {
        sendErr(
            response,
            SC_UNAUTHORIZED,
            "Authentication with machine token failed because user for this token no longer exist.");
      } catch (ServerException | JwtException e) {
        sendErr(
            response,
            SC_UNAUTHORIZED,
            format("Authentication with machine token failed cause: %s", e.getMessage()));
      }
      return null;
    }

  private Subject extractSubject(String token) throws NotFoundException, ServerException {
    final Claims claims = jwtParser.parseClaimsJws(token).getBody();
    final String userId = claims.get(USER_ID_CLAIM, String.class);
    // check if user with such id exists
    final String userName = userManager.getById(userId).getName();
    final String workspaceId = claims.get(WORKSPACE_ID_CLAIM, String.class);
    return new MachineTokenAuthorizedSubject(
        new SubjectImpl(userName, userId, token, false), permissionChecker, workspaceId);
  }

  /**
   * Sets given error code with err message into give response.
   */
  private static void sendErr(ServletResponse res, int errCode, String msg) throws IOException {
    final HttpServletResponse response = (HttpServletResponse) res;
    response.sendError(errCode, msg);
  }

}
