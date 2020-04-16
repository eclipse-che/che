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
package org.eclipse.che.api.workspace.server.model.impl;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.INTERNAL_SERVER_ATTRIBUTE;
import static org.testng.Assert.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.testng.annotations.Test;

public class ServerConfigImplTest {

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
  public void testCreateFromEndpointMinimalEndpointShouldHaveEmptyAttributes() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(new EndpointImpl("name", 123, emptyMap()));

    assertTrue(serverConfig.getAttributes().isEmpty());
  }

  @Test
  public void testCreateFromEndpointCustomAttributesShouldPreserveInAttributes() {
    Map<String, String> customAttributes = ImmutableMap.of("k1", "v1", "k2", "v2");
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(new EndpointImpl("name", 123, customAttributes));

    assertEquals(serverConfig.getAttributes().get("k1"), "v1");
    assertEquals(serverConfig.getAttributes().get("k2"), "v2");
    assertEquals(serverConfig.getAttributes().size(), 2);
  }

  @Test
  public void testCreateFromEndpointTranslatePath() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(
            new EndpointImpl("name", 123, singletonMap("path", "hello")));

    assertTrue(serverConfig.getAttributes().isEmpty());
    assertEquals(serverConfig.getPath(), "hello");
  }

  @Test
  public void testCreateFromEndpointTranslateProtocol() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(
            new EndpointImpl("name", 123, singletonMap("protocol", "hello")));

    assertTrue(serverConfig.getAttributes().isEmpty());
    assertEquals(serverConfig.getProtocol(), "hello");
  }

  @Test
  public void testCreateFromEndpointTranslatePublicTrue() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(
            new EndpointImpl("name", 123, singletonMap("public", "true")));

    assertTrue(serverConfig.getAttributes().isEmpty());
  }

  @Test
  public void testCreateFromEndpointTranslatePublicWhatever() {
    ServerConfig serverConfig =
        ServerConfigImpl.createFromEndpoint(
            new EndpointImpl("name", 123, singletonMap("public", "whatever")));

    assertTrue(serverConfig.getAttributes().isEmpty());
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
}