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
package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;

import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.AUTH_SERVER_URL_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.CLIENT_ID_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.REALM_SETTING;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

import com.google.inject.Provider;
import io.fabric8.kubernetes.client.Config;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.keycloak.server.KeycloakServiceClient;
import org.eclipse.che.multiuser.keycloak.server.KeycloakSettings;
import org.eclipse.che.multiuser.keycloak.shared.dto.KeycloakTokenResponse;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author David Festal */
@Listeners(MockitoTestNGListener.class)
public class IdentityProviderConfigFactoryTest {
  private static final String PROVIDER = "openshift-v3";
  private static final String THE_USER_ID = "a_user_id";
  private static final String ANOTHER_USER_ID = "another_user_id";
  private static final String A_WORKSPACE_ID = "workspace_id";
  private static final String FULL_SCOPE = "user:full";
  private static final String ACCESS_TOKEN = "accessToken";
  private static final String AUTH_SERVER_URL = "http://keycloak.url/auth";
  private static final String REALM = "realm";
  private static final String CLIENT_ID = "clientId";
  private static final String API_ENDPOINT = "http://che-host/api";

  private static final String SHOULD_LINK_ERROR_MESSAGE =
      "You should link your account with the <strong>"
          + PROVIDER
          + "</strong> \n"
          + "identity provider by visiting the "
          + "<a href='"
          + AUTH_SERVER_URL
          + "/realms/"
          + REALM
          + "/account/identity?referrer="
          + CLIENT_ID
          + "&referrer_uri="
          + "http%3A%2F%2Fche-host%2Fdashboard%2F%3Fredirect_fragment%3D%2Fworkspaces'"
          + " target='_blank' rel='noopener noreferrer'><strong>Federated Identities</strong></a> page of your Che account";

  private static final String SESSION_EXPIRED_MESSAGE =
      "Your session has expired. \nPlease "
          + "<a href='javascript:location.reload();' target='_top'>"
          + "login"
          + "</a> to Che again to get access to your OpenShift account";

  private static final Map<String, String> keycloakSettingsMap = new HashMap<String, String>();

  @Mock private KeycloakServiceClient keycloakServiceClient;
  @Mock private KeycloakSettings keycloakSettings;
  @Mock private Provider<WorkspaceRuntimes> workspaceRuntimeProvider;
  @Mock private WorkspaceRuntimes workspaceRuntimes;
  @Mock private Subject subject;
  @Mock private RuntimeIdentity runtimeIdentity;

  @SuppressWarnings("rawtypes")
  @Mock
  private RuntimeContext runtimeContext;

  @Mock private KeycloakTokenResponse tokenResponse;

  private EnvironmentContext context;
  private IdentityProviderConfigFactory configBuilder;
  private Config defaultConfig;

  static {
    keycloakSettingsMap.put(AUTH_SERVER_URL_SETTING, AUTH_SERVER_URL);
    keycloakSettingsMap.put(REALM_SETTING, REALM);
    keycloakSettingsMap.put(CLIENT_ID_SETTING, CLIENT_ID);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    when(keycloakSettings.get()).thenReturn(keycloakSettingsMap);
    context = spy(EnvironmentContext.getCurrent());
    EnvironmentContext.setCurrent(context);
    doReturn(subject).when(context).getSubject();
    when(workspaceRuntimeProvider.get()).thenReturn(workspaceRuntimes);
    when(workspaceRuntimes.getRuntimeContext(anyString()))
        .thenReturn(Optional.<RuntimeContext>ofNullable(runtimeContext));
    when(runtimeContext.getIdentity()).thenReturn(runtimeIdentity);
    when(runtimeIdentity.getOwnerId()).thenReturn(THE_USER_ID);
    when(subject.getUserId()).thenReturn(THE_USER_ID);
    when(tokenResponse.getScope()).thenReturn(FULL_SCOPE);
    when(tokenResponse.getAccessToken()).thenReturn(ACCESS_TOKEN);

    configBuilder =
        new IdentityProviderConfigFactory(
            keycloakServiceClient,
            keycloakSettings,
            workspaceRuntimeProvider,
            PROVIDER,
            API_ENDPOINT);
    defaultConfig = new io.fabric8.kubernetes.client.ConfigBuilder().build();
  }

  @Test
  public void testFallbackToDefaultConfigWhenProvideIsNull() throws Exception {
    when(keycloakServiceClient.getIdentityProviderToken(anyString())).thenReturn(tokenResponse);
    configBuilder =
        new IdentityProviderConfigFactory(
            keycloakServiceClient, keycloakSettings, workspaceRuntimeProvider, null, API_ENDPOINT);
    assertSame(defaultConfig, configBuilder.buildConfig(defaultConfig, A_WORKSPACE_ID));
  }

  @Test
  public void testFallbackToDefaultConfigWhenSubjectIsAnonymous() throws Exception {
    when(keycloakServiceClient.getIdentityProviderToken(anyString())).thenReturn(tokenResponse);
    doReturn(Subject.ANONYMOUS).when(context).getSubject();
    assertSame(defaultConfig, configBuilder.buildConfig(defaultConfig, A_WORKSPACE_ID));
  }

  @Test
  public void testFallbackToDefaultConfigWhenCurrentUserIsDifferentFromWorkspaceOwner()
      throws Exception {
    when(keycloakServiceClient.getIdentityProviderToken(anyString())).thenReturn(tokenResponse);
    when(runtimeIdentity.getOwnerId()).thenReturn(ANOTHER_USER_ID);
    assertSame(defaultConfig, configBuilder.buildConfig(defaultConfig, A_WORKSPACE_ID));
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testCreateUserConfigWhenNoRuntimeContext() throws Exception {
    when(keycloakServiceClient.getIdentityProviderToken(anyString())).thenReturn(tokenResponse);
    when(workspaceRuntimes.getRuntimeContext(anyString())).thenReturn(Optional.empty());

    Config resultConfig = configBuilder.buildConfig(defaultConfig, A_WORKSPACE_ID);
    assertEquals(resultConfig.getOauthToken(), ACCESS_TOKEN);
  }

  @Test
  public void testCreateUserConfigWhenWorkspaceIdIsNull() throws Exception {
    when(keycloakServiceClient.getIdentityProviderToken(anyString())).thenReturn(tokenResponse);
    Config resultConfig = configBuilder.buildConfig(defaultConfig, null);
    assertEquals(resultConfig.getOauthToken(), ACCESS_TOKEN);
  }

  @Test
  public void testCreateUserConfig() throws Exception {
    when(keycloakServiceClient.getIdentityProviderToken(anyString())).thenReturn(tokenResponse);
    Config resultConfig = configBuilder.buildConfig(defaultConfig, A_WORKSPACE_ID);
    assertEquals(resultConfig.getOauthToken(), ACCESS_TOKEN);
  }

  @Test(expectedExceptions = {InfrastructureException.class})
  public void testThrowOnBadScope() throws Exception {
    when(keycloakServiceClient.getIdentityProviderToken(anyString())).thenReturn(tokenResponse);
    when(tokenResponse.getScope()).thenReturn("bad:scope");
    Config resultConfig = configBuilder.buildConfig(defaultConfig, A_WORKSPACE_ID);
    assertEquals(resultConfig.getOauthToken(), ACCESS_TOKEN);
  }

  @Test
  public void testRethrowOnUnauthorizedException() throws Exception {
    doThrow(
            new UnauthorizedException(
                DtoFactory.newDto(ServiceError.class).withMessage("Any other message")))
        .when(keycloakServiceClient)
        .getIdentityProviderToken(anyString());
    try {
      configBuilder.buildConfig(defaultConfig, A_WORKSPACE_ID);
    } catch (InfrastructureException e) {
      assertEquals(e.getMessage(), SHOULD_LINK_ERROR_MESSAGE, "The exception message is wrong");
      return;
    }
    fail(
        "Should have thrown an exception with the following message: " + SHOULD_LINK_ERROR_MESSAGE);
  }

  @Test(expectedExceptions = {InfrastructureException.class})
  public void testRethrowOnBadRequestException() throws Exception {
    doThrow(
            new BadRequestException(
                DtoFactory.newDto(ServiceError.class).withMessage("Any other message")))
        .when(keycloakServiceClient)
        .getIdentityProviderToken(anyString());
    configBuilder.buildConfig(defaultConfig, A_WORKSPACE_ID);
  }

  @Test
  public void testRethrowOnInvalidTokenBadRequestException() throws Exception {
    doThrow(
            new BadRequestException(
                DtoFactory.newDto(ServiceError.class).withMessage("Invalid token.")))
        .when(keycloakServiceClient)
        .getIdentityProviderToken(anyString());
    try {
      configBuilder.buildConfig(defaultConfig, A_WORKSPACE_ID);
    } catch (InfrastructureException e) {
      assertEquals(e.getMessage(), SESSION_EXPIRED_MESSAGE, "The exception message is wrong");
      return;
    }
    fail("Should have thrown an exception with the following message: " + SESSION_EXPIRED_MESSAGE);
  }

  @Test(expectedExceptions = {InfrastructureException.class})
  public void testRethrowOnAnyException() throws Exception {
    when(keycloakServiceClient.getIdentityProviderToken(anyString()))
        .thenThrow(org.eclipse.che.api.core.NotFoundException.class);
    configBuilder.buildConfig(defaultConfig, A_WORKSPACE_ID);
  }
}
