/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfig;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfigs;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
@Listeners(MockitoTestNGListener.class)
public class DockerRegistryAuthResolverTest {

  private static final String INITIAL_REGISTRY1_URL = "test.registry.com:5050";
  private static final String INITIAL_REGISTRY2_URL = "some.reg:1234";
  private static final String INITIAL_REGISTRY3_URL = "somehost:4567";
  private static final String INITIAL_REGISTRY1_USERNAME = "user1";
  private static final String INITIAL_REGISTRY2_USERNAME = "login2";
  private static final String INITIAL_REGISTRY3_USERNAME = "username1234";
  private static final String INITIAL_REGISTRY1_PASSWORD = "1234";
  private static final String INITIAL_REGISTRY2_PASSWORD = "abcd";
  private static final String INITIAL_REGISTRY3_PASSWORD = "a1b2c3d4";

  private static final String INITIAL_REGISTRY1_X_REGISTRY_AUTH_HEADER_VALUE =
      "{\"username\":\"user1\",\"password\":\"1234\"}";
  private static final String INITIAL_REGISTRY2_X_REGISTRY_AUTH_HEADER_VALUE =
      "{\"username\":\"login2\",\"password\":\"abcd\"}";
  private static final String INITIAL_REGISTRY3_X_REGISTRY_AUTH_HEADER_VALUE =
      "{\"username\":\"username1234\",\"password\":\"a1b2c3d4\"}";

  private static final String REGISTRY1_URL = "user.registry.com:5000";
  private static final String REGISTRY2_URL = "local.registry:1234";
  private static final String REGISTRY3_URL = INITIAL_REGISTRY1_URL;
  private static final String REGISTRY1_USERNAME = "user";
  private static final String REGISTRY2_USERNAME = "mylocallogin";
  private static final String REGISTRY3_USERNAME = "usrname";
  private static final String REGISTRY1_PASSWORD = "pass";
  private static final String REGISTRY2_PASSWORD = "localreg";
  private static final String REGISTRY3_PASSWORD = "pass1234";

  private static final String REGISTRY1_X_REGISTRY_AUTH_HEADER_VALUE =
      "{\"username\":\"user\",\"password\":\"pass\"}";
  private static final String REGISTRY2_X_REGISTRY_AUTH_HEADER_VALUE =
      "{\"username\":\"mylocallogin\",\"password\":\"localreg\"}";
  private static final String REGISTRY3_X_REGISTRY_AUTH_HEADER_VALUE =
      "{\"username\":\"usrname\",\"password\":\"pass1234\"}";

  private static final String REGISTRY_WITH_DYNAMIC_PASSWORD_URL = "some.registry.com:1234";
  private static final String REGISTRY_WITH_DYNAMIC_PASSWORD_USERNAME = "USR";
  private static final String REGISTRY_WITH_DYNAMIC_PASSWORD_PASSWORD = "<current time>";

  private static final String REGISTRY_WITH_DYNAMIC_PASSWORD_AUTH_HEADER_VALUE =
      "{\"username\":\"USR\",\"password\":\"<current time>\"}";

  private static final String INITIAL_X_AUTH_CONFIG_VALUE =
      "{\"test.registry.com:5050\":{\"password\":\"1234\",\"username\":\"user1\"},"
          + "\"somehost:4567\":{\"password\":\"a1b2c3d4\",\"username\":\"username1234\"},"
          + "\"some.reg:1234\":{\"password\":\"abcd\",\"username\":\"login2\"}}";
  private static final String CUSTOM_X_AUTH_CONFIG_VALUE =
      "{\"local.registry:1234\":{\"password\":\"localreg\",\"username\":\"mylocallogin\"},"
          + "\"test.registry.com:5050\":{\"password\":\"pass1234\",\"username\":\"usrname\"},"
          + "\"user.registry.com:5000\":{\"password\":\"pass\",\"username\":\"user\"}}";
  private static final String REGISTRY_WITH_DYNAMIC_PASSWORD_X_AUTH_CONFIG_VALUE =
      "{\"some.registry.com:1234\":{\"password\":\"<current time>\",\"username\":\"USR\"}}";
  private static final String X_AUTH_CONFIG_VALUE =
      "{\"local.registry:1234\":{\"password\":\"localreg\",\"username\":\"mylocallogin\"},"
          + "\"test.registry.com:5050\":{\"password\":\"pass1234\",\"username\":\"usrname\"},"
          + "\"somehost:4567\":{\"password\":\"a1b2c3d4\",\"username\":\"username1234\"},"
          + "\"some.reg:1234\":{\"password\":\"abcd\",\"username\":\"login2\"},"
          + "\"user.registry.com:5000\":{\"password\":\"pass\",\"username\":\"user\"}}";

  private static final String DEFAULT_REGISTRY_URL_ALIAS1 = null;
  private static final String DEFAULT_REGISTRY_URL_ALIAS2 = "";
  private static final String DEFAULT_REGISTRY_URL_ALIAS3 = "docker.io";
  private static final String DEFAULT_REGISTRY_URL_ALIAS4 = "index.docker.io";
  private static final String DEFAULT_REGISTRY_URL = "https://index.docker.io/v1/";
  private static final String DEFAULT_REGISTRY_USERNAME = "dockerHubUser";
  private static final String DEFAULT_REGISTRY_PASSWORD = "passwordFromDockerHubAccount";

  private static final String DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE =
      "{\"username\":\"dockerHubUser\",\"password\":\"passwordFromDockerHubAccount\"}";
  private static final String DEFAULT_REGISTRY_X_AUTH_CONFIG_VALUE =
      "{\"https://index.docker.io/v1/\":{\"password\":\"passwordFromDockerHubAccount\",\"username\":\"dockerHubUser\"}}";

  private static final String UNKNOWN_REGISTRY = "unknown.registry.com:1007";
  private static final String EMPTY_JSON = "{}";

  @Mock private InitialAuthConfig initialAuthConfig;
  @Mock private DockerRegistryDynamicAuthResolver dynamicAuthResolver;

  private AuthConfigs initialAuthConfigs;
  private AuthConfigs customAuthConfigs;
  private AuthConfigs dynamicAuthConfigs;
  private AuthConfigs emptyAuthConfigs;
  private AuthConfigs dockerHubAuthConfigs;

  @InjectMocks private DockerRegistryAuthResolver authResolver;

  @BeforeClass
  private void before() {
    Map<String, AuthConfig> initialAuthConfigsMap = new HashMap<>();
    initialAuthConfigsMap.put(
        INITIAL_REGISTRY1_URL,
        DtoFactory.newDto(AuthConfig.class)
            .withUsername(INITIAL_REGISTRY1_USERNAME)
            .withPassword(INITIAL_REGISTRY1_PASSWORD));
    initialAuthConfigsMap.put(
        INITIAL_REGISTRY2_URL,
        DtoFactory.newDto(AuthConfig.class)
            .withUsername(INITIAL_REGISTRY2_USERNAME)
            .withPassword(INITIAL_REGISTRY2_PASSWORD));
    initialAuthConfigsMap.put(
        INITIAL_REGISTRY3_URL,
        DtoFactory.newDto(AuthConfig.class)
            .withUsername(INITIAL_REGISTRY3_USERNAME)
            .withPassword(INITIAL_REGISTRY3_PASSWORD));
    initialAuthConfigs = DtoFactory.newDto(AuthConfigs.class).withConfigs(initialAuthConfigsMap);

    Map<String, AuthConfig> customAuthConfigsMap = new HashMap<>();
    customAuthConfigsMap.put(
        REGISTRY1_URL,
        DtoFactory.newDto(AuthConfig.class)
            .withUsername(REGISTRY1_USERNAME)
            .withPassword(REGISTRY1_PASSWORD));
    customAuthConfigsMap.put(
        REGISTRY2_URL,
        DtoFactory.newDto(AuthConfig.class)
            .withUsername(REGISTRY2_USERNAME)
            .withPassword(REGISTRY2_PASSWORD));
    customAuthConfigsMap.put(
        REGISTRY3_URL,
        DtoFactory.newDto(AuthConfig.class)
            .withUsername(REGISTRY3_USERNAME)
            .withPassword(REGISTRY3_PASSWORD));
    customAuthConfigs = DtoFactory.newDto(AuthConfigs.class).withConfigs(customAuthConfigsMap);

    Map<String, AuthConfig> dynamicAuthConfigsMap = new HashMap<>();
    dynamicAuthConfigsMap.put(
        REGISTRY_WITH_DYNAMIC_PASSWORD_URL,
        DtoFactory.newDto(AuthConfig.class)
            .withUsername(REGISTRY_WITH_DYNAMIC_PASSWORD_USERNAME)
            .withPassword(REGISTRY_WITH_DYNAMIC_PASSWORD_PASSWORD));
    dynamicAuthConfigs = DtoFactory.newDto(AuthConfigs.class).withConfigs(dynamicAuthConfigsMap);

    emptyAuthConfigs = DtoFactory.newDto(AuthConfigs.class).withConfigs(new HashMap<>());

    Map<String, AuthConfig> dockerHubAuthConfigMap = new HashMap<>();
    dockerHubAuthConfigMap.put(
        DEFAULT_REGISTRY_URL,
        DtoFactory.newDto(AuthConfig.class)
            .withUsername(DEFAULT_REGISTRY_USERNAME)
            .withPassword(DEFAULT_REGISTRY_PASSWORD));
    dockerHubAuthConfigs = DtoFactory.newDto(AuthConfigs.class).withConfigs(dockerHubAuthConfigMap);
  }

  @BeforeMethod
  private void setup() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(initialAuthConfigs);
  }

  @Test
  public void shouldGetXRegistryAuthHeaderValueFromInitialAuthConfigOnly1() {
    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(INITIAL_REGISTRY1_URL, null);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(INITIAL_REGISTRY1_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryAuthHeaderValueFromInitialAuthConfigOnly2() {
    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(INITIAL_REGISTRY2_URL, null);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(INITIAL_REGISTRY2_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryAuthHeaderValueFromInitialAuthConfigOnly3() {
    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(INITIAL_REGISTRY3_URL, null);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(INITIAL_REGISTRY3_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void
      shouldReturnEmptyEncodedJsonIfRegistryNotFoundInInitialAuthConfigAndCustomConfigIsNull() {
    String base64HeaderValue = authResolver.getXRegistryAuthHeaderValue(UNKNOWN_REGISTRY, null);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue), jsonToAuthConfig(EMPTY_JSON));
  }

  @Test
  public void shouldGetXRegistryAuthHeaderValueFromCustomConfig1() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(REGISTRY1_URL, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(REGISTRY1_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryAuthHeaderValueFromCustomConfig2() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(REGISTRY2_URL, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(REGISTRY2_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryAuthHeaderValueFromCustomConfig3() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(REGISTRY3_URL, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(REGISTRY3_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void
      shouldReturnEmptyEncodedJsonIfRegistryNotFoundInCustomAuthConfigAndInitialAuthConfigIsNull() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(UNKNOWN_REGISTRY, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue), jsonToAuthConfig(EMPTY_JSON));
  }

  @Test
  public void
      shouldGetXRegistryAuthConfigFromCustomAuthConfigIfSpecifiedRegistryExistsInBothConfigs() {
    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(INITIAL_REGISTRY1_URL, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(REGISTRY3_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryAuthConfigValue2() {
    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(INITIAL_REGISTRY2_URL, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(INITIAL_REGISTRY2_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryAuthConfigValue3() {
    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(INITIAL_REGISTRY3_URL, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(INITIAL_REGISTRY3_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryAuthConfigValue4() {
    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(REGISTRY1_URL, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(REGISTRY1_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryAuthConfigValue5() {
    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(REGISTRY2_URL, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(REGISTRY2_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryAuthConfigValue6() {
    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(REGISTRY3_URL, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(REGISTRY3_X_REGISTRY_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldReturnEmptyEncodedJsonIfRegistryNotFound() {
    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(UNKNOWN_REGISTRY, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue), jsonToAuthConfig(EMPTY_JSON));
  }

  @Test
  public void shouldAcceptDockerHubAlias1WhenGetXRegistryAuthValueFromInitialConfig() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(dockerHubAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS1, null);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void shouldAcceptDockerHubAlias2WhenGetXRegistryAuthValueFromInitialConfig() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(dockerHubAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS2, null);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void shouldAcceptDockerHubAlias3WhenGetXRegistryAuthValueFromInitialConfig() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(dockerHubAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS3, null);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void shouldAcceptDockerHubAlias4WhenGetXRegistryAuthValueFromInitialConfig() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(dockerHubAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS4, null);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void
      shouldGetXRegistryAuthValueFromInitialConfigWhenDockerHubRegistryConfiguredWithAlias1() {
    AuthConfigs dockerHubAuthConfigs =
        modifyAuthConfigUrl(
            this.dockerHubAuthConfigs, DEFAULT_REGISTRY_URL, DEFAULT_REGISTRY_URL_ALIAS3);
    when(initialAuthConfig.getAuthConfigs()).thenReturn(dockerHubAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS1, null);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void
      shouldGetXRegistryAuthValueFromInitialConfigWhenDockerHubRegistryConfiguredWithAlias2() {
    AuthConfigs dockerHubAuthConfigs =
        modifyAuthConfigUrl(
            this.dockerHubAuthConfigs, DEFAULT_REGISTRY_URL, DEFAULT_REGISTRY_URL_ALIAS4);
    when(initialAuthConfig.getAuthConfigs()).thenReturn(dockerHubAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS1, null);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void shouldAcceptDockerHubAlias1WhenGetXRegistryAuthValueFromCustomConfig() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS1, dockerHubAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void shouldAcceptDockerHubAlias2WhenGetXRegistryAuthValueFromCustomConfig() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS2, dockerHubAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void shouldAcceptDockerHubAlias3WhenGetXRegistryAuthValueFromCustomConfig() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS3, dockerHubAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void shouldAcceptDockerHubAlias4WhenGetXRegistryAuthValueFromCustomConfig() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS4, dockerHubAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void
      shouldGetXRegistryAuthValueFromCustomConfigWhenDockerHubRegistryConfiguredWithAlias1() {
    AuthConfigs dockerHubAuthConfigs =
        modifyAuthConfigUrl(
            this.dockerHubAuthConfigs, DEFAULT_REGISTRY_URL, DEFAULT_REGISTRY_URL_ALIAS3);
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS1, dockerHubAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void
      shouldGetXRegistryAuthValueFromCustomConfigWhenDockerHubRegistryConfiguredWithAlias2() {
    AuthConfigs dockerHubAuthConfigs =
        modifyAuthConfigUrl(
            this.dockerHubAuthConfigs, DEFAULT_REGISTRY_URL, DEFAULT_REGISTRY_URL_ALIAS4);
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(DEFAULT_REGISTRY_URL_ALIAS1, dockerHubAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(DEFAULT_REGISTRY_X_REGISTRY_AUTH_VALUE));
  }

  @Test
  public void shouldGetXRegistryConfigValueFromInitialAuthConfigIfCustomAuthConfigIsNull() {
    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(null);

    assertEqualsXRegistryConfigHeader(
        base64ToAuthConfigs(base64HeaderValue), jsonToAuthConfigs(INITIAL_X_AUTH_CONFIG_VALUE));
  }

  @Test
  public void shouldGetXRegistryConfigValueFromCustomAuthConfigIfInitialAuthConfigIsNull() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(customAuthConfigs);

    assertEqualsXRegistryConfigHeader(
        base64ToAuthConfigs(base64HeaderValue), jsonToAuthConfigs(CUSTOM_X_AUTH_CONFIG_VALUE));
  }

  @Test
  public void shouldGetXRegistryConfigValue() {
    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(customAuthConfigs);

    assertEqualsXRegistryConfigHeader(
        base64ToAuthConfigs(base64HeaderValue), jsonToAuthConfigs(X_AUTH_CONFIG_VALUE));
  }

  @Test
  public void
      shouldReplaceDockerHubAliasDockerIoToFullUrlWhenGetXRegistryConfigValueFromInitialConfig() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(dockerHubAuthConfigs);

    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(null);

    assertEqualsXRegistryConfigHeader(
        base64ToAuthConfigs(base64HeaderValue),
        jsonToAuthConfigs(DEFAULT_REGISTRY_X_AUTH_CONFIG_VALUE));
  }

  @Test
  public void
      shouldReplaceDockerHubAliasDockerIoToFullUrlWhenGetXRegistryConfigValueFromCustomConfig() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(dockerHubAuthConfigs);

    assertEqualsXRegistryConfigHeader(
        base64ToAuthConfigs(base64HeaderValue),
        jsonToAuthConfigs(DEFAULT_REGISTRY_X_AUTH_CONFIG_VALUE));
  }

  @Test
  public void
      shouldGetXRegistryConfigValueFromInitialConfigWhenDockerHubRegistryConfiguredWithAlias1() {
    AuthConfigs initialAuthConfigs = copyAuthConfigs(this.initialAuthConfigs);
    initialAuthConfigs
        .getConfigs()
        .put(
            DEFAULT_REGISTRY_URL_ALIAS3,
            dockerHubAuthConfigs.getConfigs().get(DEFAULT_REGISTRY_URL));
    when(initialAuthConfig.getAuthConfigs()).thenReturn(initialAuthConfigs);

    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(null);

    assertTrue(
        base64ToAuthConfigs(base64HeaderValue).getConfigs().containsKey(DEFAULT_REGISTRY_URL));
  }

  @Test
  public void
      shouldGetXRegistryConfigValueFromInitialConfigWhenDockerHubRegistryConfiguredWithAlias2() {
    AuthConfigs initialAuthConfigs = copyAuthConfigs(this.initialAuthConfigs);
    initialAuthConfigs
        .getConfigs()
        .put(
            DEFAULT_REGISTRY_URL_ALIAS4,
            dockerHubAuthConfigs.getConfigs().get(DEFAULT_REGISTRY_URL));
    when(initialAuthConfig.getAuthConfigs()).thenReturn(initialAuthConfigs);

    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(null);

    assertTrue(
        base64ToAuthConfigs(base64HeaderValue).getConfigs().containsKey(DEFAULT_REGISTRY_URL));
  }

  @Test
  public void
      shouldGetXRegistryConfigValueWithDockerHubCredentialsFromCustomConfigWhenDockerHubRegistryConfiguredWithAlias1() {
    AuthConfigs dockerHubAuthConfigs = copyAuthConfigs(this.dockerHubAuthConfigs);
    dockerHubAuthConfigs
        .getConfigs()
        .put(
            DEFAULT_REGISTRY_URL_ALIAS3,
            dockerHubAuthConfigs.getConfigs().remove(DEFAULT_REGISTRY_URL));

    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(dockerHubAuthConfigs);

    assertTrue(
        base64ToAuthConfigs(base64HeaderValue).getConfigs().containsKey(DEFAULT_REGISTRY_URL));
  }

  @Test
  public void
      shouldGetXRegistryConfigValueWithDockerHubCredentialsFromCustomConfigWhenDockerHubRegistryConfiguredWithAlias2() {
    AuthConfigs dockerHubAuthConfigs = copyAuthConfigs(this.dockerHubAuthConfigs);
    dockerHubAuthConfigs
        .getConfigs()
        .put(
            DEFAULT_REGISTRY_URL_ALIAS4,
            dockerHubAuthConfigs.getConfigs().remove(DEFAULT_REGISTRY_URL));

    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(dockerHubAuthConfigs);

    assertTrue(
        base64ToAuthConfigs(base64HeaderValue).getConfigs().containsKey(DEFAULT_REGISTRY_URL));
  }

  @Test
  public void shouldReturnEmptyEncodedJsonIfNoRegistryConfigured() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);

    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(null);

    assertEqualsXRegistryConfigHeader(
        base64ToAuthConfigs(base64HeaderValue), jsonToAuthConfigs(EMPTY_JSON));
  }

  @Test
  public void shouldGetXRegistryAuthHeaderValueFromDynamicConfigOnly() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);
    when(dynamicAuthResolver.getXRegistryAuth(REGISTRY_WITH_DYNAMIC_PASSWORD_URL))
        .thenReturn(dynamicAuthConfigs.getConfigs().get(REGISTRY_WITH_DYNAMIC_PASSWORD_URL));

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(
            REGISTRY_WITH_DYNAMIC_PASSWORD_URL, emptyAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(REGISTRY_WITH_DYNAMIC_PASSWORD_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryAuthHeaderValueFromDynamicConfig() {
    when(dynamicAuthResolver.getXRegistryAuth(REGISTRY_WITH_DYNAMIC_PASSWORD_URL))
        .thenReturn(dynamicAuthConfigs.getConfigs().get(REGISTRY_WITH_DYNAMIC_PASSWORD_URL));

    String base64HeaderValue =
        authResolver.getXRegistryAuthHeaderValue(
            REGISTRY_WITH_DYNAMIC_PASSWORD_URL, customAuthConfigs);

    assertEqualsXRegistryAuthHeader(
        base64ToAuthConfig(base64HeaderValue),
        jsonToAuthConfig(REGISTRY_WITH_DYNAMIC_PASSWORD_AUTH_HEADER_VALUE));
  }

  @Test
  public void shouldGetXRegistryConfigValueFromDynamicAuthConfigOnly() {
    when(initialAuthConfig.getAuthConfigs()).thenReturn(emptyAuthConfigs);
    when(dynamicAuthResolver.getXRegistryConfig()).thenReturn(dynamicAuthConfigs.getConfigs());

    String base64HeaderValue = authResolver.getXRegistryConfigHeaderValue(emptyAuthConfigs);

    assertEqualsXRegistryConfigHeader(
        base64ToAuthConfigs(base64HeaderValue),
        jsonToAuthConfigs(REGISTRY_WITH_DYNAMIC_PASSWORD_X_AUTH_CONFIG_VALUE));
  }

  @Test
  public void shouldGetBasicAuthHeaderValueFromInitialConfig() {
    assertEquals(
        authResolver.getBasicAuthHeaderValue(INITIAL_REGISTRY2_URL, null),
        "Basic "
            + Base64.getEncoder()
                .encodeToString(
                    (INITIAL_REGISTRY2_USERNAME + ':' + INITIAL_REGISTRY2_PASSWORD).getBytes()));
  }

  @Test
  public void shouldGetBasicAuthHeaderValueFromCustomConfig() {
    assertEquals(
        authResolver.getBasicAuthHeaderValue(REGISTRY2_URL, customAuthConfigs),
        "Basic "
            + Base64.getEncoder()
                .encodeToString((REGISTRY2_USERNAME + ':' + REGISTRY2_PASSWORD).getBytes()));
  }

  @Test
  public void shouldGetBasicAuthHeaderValueFromDynamicConfig() {
    when(dynamicAuthResolver.getXRegistryAuth(REGISTRY_WITH_DYNAMIC_PASSWORD_URL))
        .thenReturn(dynamicAuthConfigs.getConfigs().get(REGISTRY_WITH_DYNAMIC_PASSWORD_URL));

    assertEquals(
        authResolver.getBasicAuthHeaderValue(REGISTRY_WITH_DYNAMIC_PASSWORD_URL, customAuthConfigs),
        "Basic "
            + Base64.getEncoder()
                .encodeToString(
                    (REGISTRY_WITH_DYNAMIC_PASSWORD_USERNAME
                            + ':'
                            + REGISTRY_WITH_DYNAMIC_PASSWORD_PASSWORD)
                        .getBytes()));
  }

  @Test
  public void shouldReturnEmptyStringIfRegistryNotConfigured() {
    assertEquals(
        authResolver.getBasicAuthHeaderValue("unconfigured.registry.com:5000", customAuthConfigs),
        "");
  }

  private AuthConfig base64ToAuthConfig(String base64decodedJson) {
    return jsonToAuthConfig(new String(Base64.getDecoder().decode(base64decodedJson.getBytes())));
  }

  private AuthConfig jsonToAuthConfig(String json) {
    return DtoFactory.getInstance().createDtoFromJson(json, AuthConfig.class);
  }

  private void assertEqualsXRegistryAuthHeader(AuthConfig actual, AuthConfig expected) {
    assertEquals(actual.getUsername(), expected.getUsername());
    assertEquals(actual.getPassword(), expected.getPassword());
  }

  private AuthConfigs base64ToAuthConfigs(String base64decodedJson) {
    return jsonToAuthConfigs(new String(Base64.getDecoder().decode(base64decodedJson.getBytes())));
  }

  private AuthConfigs jsonToAuthConfigs(String json) {
    return DtoFactory.getInstance()
        .createDtoFromJson("{\"configs\":" + json + '}', AuthConfigs.class);
  }

  private void assertEqualsXRegistryConfigHeader(AuthConfigs actual, AuthConfigs expected) {
    assertEquals(actual.getConfigs(), expected.getConfigs());
  }

  private AuthConfigs copyAuthConfigs(AuthConfigs origin) {
    Map<String, AuthConfig> authConfigMap = new HashMap<>(origin.getConfigs());
    return DtoFactory.newDto(AuthConfigs.class).withConfigs(authConfigMap);
  }

  private AuthConfigs modifyAuthConfigUrl(AuthConfigs authConfigs, String oldKey, String newKey) {
    Map<String, AuthConfig> authConfigMap = new HashMap<>(authConfigs.getConfigs());
    authConfigMap.put(newKey, authConfigMap.remove(oldKey));
    return DtoFactory.newDto(AuthConfigs.class).withConfigs(authConfigMap);
  }
}
