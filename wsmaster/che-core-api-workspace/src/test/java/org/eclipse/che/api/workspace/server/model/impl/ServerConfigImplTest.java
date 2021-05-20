/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.model.impl;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.INTERNAL_SERVER_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.REQUIRE_SUBDOMAIN;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVER_NAME_ATTRIBUTE;
import static org.testng.Assert.*;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.testng.annotations.Test;

public class ServerConfigImplTest {

  @Test
  public void testStoreEndpointNameIntoAttributes() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(new EndpointImpl("blabol", 123, emptyMap()));

    assertEquals(serverConfig.getAttributes().get(SERVER_NAME_ATTRIBUTE), "blabol");
  }

  @Test
  public void testCreateFromEndpointMinimalEndpointShouldTranslateToHttpProtocol() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(new EndpointImpl("name", 123, emptyMap()));

    assertEquals(serverConfig.getProtocol(), "http");
  }

  @Test
  public void testCreateFromEndpointMinimalEndpointShouldTranslateToNullPath() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(new EndpointImpl("name", 123, emptyMap()));

    assertNull(serverConfig.getPath());
  }

  @Test
  public void testCreateFromEndpointCustomAttributesShouldPreserveInAttributes() {
    Map<String, String> customAttributes = ImmutableMap.of("k1", "v1", "k2", "v2");
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(new EndpointImpl("name", 123, customAttributes));

    assertEquals(serverConfig.getAttributes().get("k1"), "v1");
    assertEquals(serverConfig.getAttributes().get("k2"), "v2");
    assertEquals(serverConfig.getAttributes().size(), 3);
  }

  @Test
  public void testCreateFromEndpointTranslatePath() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(
            new EndpointImpl("name", 123, singletonMap("path", "hello")));

    assertEquals(serverConfig.getPath(), "hello");
  }

  @Test
  public void testCreateFromEndpointTranslateProtocol() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(
            new EndpointImpl("name", 123, singletonMap("protocol", "hello")));

    assertEquals(serverConfig.getProtocol(), "hello");
  }

  @Test
  public void testCreateFromEndpointTranslatePublicTrue() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(
            new EndpointImpl("name", 123, singletonMap("public", "true")));

    assertFalse(serverConfig.isInternal());
  }

  @Test
  public void testCreateFromEndpointTranslatePublicWhatever() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(
            new EndpointImpl("name", 123, singletonMap("public", "whatever")));

    assertFalse(serverConfig.isInternal());
  }

  @Test
  public void testCreateFromEndpointTranslatePublicFalse() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(
            new EndpointImpl("name", 123, singletonMap("public", "false")));

    assertFalse(serverConfig.getAttributes().isEmpty());
    assertEquals(
        serverConfig.getAttributes().get(INTERNAL_SERVER_ATTRIBUTE), Boolean.TRUE.toString());
  }

  @Test
  public void testCreateFromEndpointDevfileEndpointAttributeSet() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(new EndpointImpl("name", 123, new HashMap<>()), true);

    assertTrue(serverConfig.getAttributes().containsKey(REQUIRE_SUBDOMAIN));
    assertTrue(Boolean.parseBoolean(serverConfig.getAttributes().get(REQUIRE_SUBDOMAIN)));
  }

  @Test
  public void testCreateFromEndpointDevfileEndpointAttributeNotSet() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(new EndpointImpl("name", 123, new HashMap<>()), false);

    assertFalse(serverConfig.getAttributes().containsKey(REQUIRE_SUBDOMAIN));
  }

  @Test
  public void testCreateFromEndpointDevfileEndpointAttributeNotSetWhenDefault() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(new EndpointImpl("name", 123, new HashMap<>()));

    assertFalse(serverConfig.getAttributes().containsKey(REQUIRE_SUBDOMAIN));
  }
}
