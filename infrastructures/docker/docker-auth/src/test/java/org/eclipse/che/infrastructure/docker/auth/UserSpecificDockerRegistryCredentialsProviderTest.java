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
package org.eclipse.che.infrastructure.docker.auth;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfig;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfigs;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
@Listeners(MockitoTestNGListener.class)
public class UserSpecificDockerRegistryCredentialsProviderTest {

  private static final String DOCKER_REGISTRY_CREDENTIALS_KEY = "dockerCredentials";

  @Mock private PreferenceManager preferenceManager;

  private UserSpecificDockerRegistryCredentialsProvider dockerCredentials;

  @BeforeClass
  private void before() {
    dockerCredentials = new UserSpecificDockerRegistryCredentialsProvider(preferenceManager);
  }

  @Test
  public void shouldParseCredentialsFromUserPreferences() throws ServerException {
    String base64encodedCredentials =
        "eyJyZWdpc3RyeS5jb206NTAwMCI6eyJ1c2VybmFtZSI6ImxvZ2luIiwicGFzc3dvcmQiOiJwYXNzIn19";
    setCredentialsIntoPreferences(base64encodedCredentials);
    String registry = "registry.com:5000";
    AuthConfig authConfig =
        DtoFactory.newDto(AuthConfig.class).withUsername("login").withPassword("pass");

    AuthConfigs parsedAuthConfigs = dockerCredentials.getCredentials();

    AuthConfig parsedAuthConfig = parsedAuthConfigs.getConfigs().get(registry);

    assertNotNull(parsedAuthConfig);
    assertEquals(parsedAuthConfig.getUsername(), authConfig.getUsername());
    assertEquals(parsedAuthConfig.getPassword(), authConfig.getPassword());
  }

  @Test
  public void shouldReturnNullIfDataFormatIsCorruptedInPreferences() throws ServerException {
    String base64encodedCredentials = "sdJfpwJwkek59kafj239lFfkHjhek5l1";
    setCredentialsIntoPreferences(base64encodedCredentials);

    AuthConfigs parsedAuthConfigs = dockerCredentials.getCredentials();

    assertNull(parsedAuthConfigs);
  }

  @Test
  public void shouldReturnNullIfDataFormatIsWrong() throws ServerException {
    String base64encodedCredentials = "eyJpbnZhbGlkIjoianNvbiJ9";
    setCredentialsIntoPreferences(base64encodedCredentials);

    AuthConfigs parsedAuthConfigs = dockerCredentials.getCredentials();

    assertNull(parsedAuthConfigs);
  }

  @Test
  public void shouldReturnConfigsWithEmptyMapIfNoCredentialsDataInUserPreferences()
      throws ServerException {
    String base64encodedCredentials = "e30=";
    setCredentialsIntoPreferences(base64encodedCredentials);

    AuthConfigs parsedAuthConfigs = dockerCredentials.getCredentials();

    assertEquals(parsedAuthConfigs.getConfigs().size(), 0);
  }

  private void setCredentialsIntoPreferences(String base64encodedCredentials)
      throws ServerException {
    Map<String, String> preferences = new HashMap<>();
    preferences.put(DOCKER_REGISTRY_CREDENTIALS_KEY, base64encodedCredentials);

    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("name", "id", "token1234", false));
    when(preferenceManager.find(anyObject(), anyObject())).thenReturn(preferences);
  }
}
