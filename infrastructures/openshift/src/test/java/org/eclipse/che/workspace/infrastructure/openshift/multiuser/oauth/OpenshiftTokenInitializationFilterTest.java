package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.token.RequestTokenExtractor;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class OpenshiftTokenInitializationFilterTest {
  @Mock private SessionStore sessionStore;
  @Mock private RequestTokenExtractor tokenExtractor;
  @Mock private UserManager userManager;
  @Mock private PermissionChecker permissionChecker;

  @Mock private OpenShiftClientFactory openShiftClientFactory;
  @Mock private OpenShiftClient openShiftClient;
  @Mock private User openshiftUser;
  @Mock private ObjectMeta openshiftUserMeta;

  @Mock private HttpServletRequest servletRequest;
  @Mock private ServletResponse servletResponse;
  @Mock private FilterChain filterChain;

  private static final String TOKEN = "touken";
  private static final String USER_UID = "almost-certainly-unique-id";
  private static final String USERNAME = "test_username";

  private OpenshiftTokenInitializationFilter openshiftTokenInitializationFilter;

  @BeforeMethod
  public void setUp() throws InfrastructureException {
    openshiftTokenInitializationFilter =
        new OpenshiftTokenInitializationFilter(
            sessionStore, tokenExtractor, openShiftClientFactory, userManager, permissionChecker);
  }

  @Test
  public void getUserIdGetsCurrentUserWithAuthenticatedOCClient() throws InfrastructureException {
    when(openShiftClientFactory.createAuthenticatedOC(TOKEN)).thenReturn(openShiftClient);
    when(openShiftClient.currentUser()).thenReturn(openshiftUser);
    when(openshiftUser.getMetadata()).thenReturn(openshiftUserMeta);
    when(openshiftUserMeta.getUid()).thenReturn(USER_UID);

    String userId = openshiftTokenInitializationFilter.getUserId(TOKEN);

    assertEquals(userId, USER_UID);
    verify(openShiftClientFactory).createAuthenticatedOC(TOKEN);
    verify(openShiftClient).currentUser();
  }

  @Test
  public void extractSubjectCreatesSubjectWithCurretlyAuthenticatedUser()
      throws InfrastructureException, ServerException, ConflictException {
    when(openShiftClientFactory.createAuthenticatedOC(TOKEN)).thenReturn(openShiftClient);
    when(openShiftClient.currentUser()).thenReturn(openshiftUser);
    when(openshiftUser.getMetadata()).thenReturn(openshiftUserMeta);
    when(openshiftUserMeta.getUid()).thenReturn(USER_UID);
    when(openshiftUserMeta.getName()).thenReturn(USERNAME);
    when(userManager.getOrCreateUser(USER_UID, USERNAME + "@che", USERNAME))
        .thenReturn(new UserImpl(USER_UID, USERNAME + "@che", USERNAME));

    Subject subject = openshiftTokenInitializationFilter.extractSubject(TOKEN);

    assertEquals(subject.getUserId(), USER_UID);
    assertEquals(subject.getUserName(), USERNAME);
  }

  @Test
  public void handleMissingTokenShouldAllowUnauthorizedEndpoint()
      throws ServletException, IOException {
    when(servletRequest.getServletPath()).thenReturn("blabol");

    openshiftTokenInitializationFilter.handleMissingToken(
        servletRequest, servletResponse, filterChain);

    verify(filterChain).doFilter(servletRequest, servletResponse);
  }
}
