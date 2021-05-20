/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/**
 * Tests {@link JwtProxyConfigBuilder}.
 *
 * @author Sergii Leshchenko
 */
public class JwtProxyConfigBuilderTest {

  private JwtProxyConfigBuilder jwtProxyConfigBuilder;

  @BeforeMethod
  public void setUp() {
    jwtProxyConfigBuilder =
        new JwtProxyConfigBuilder(
            URI.create("http://che-host.com"),
            "wsmaster",
            "1m",
            "/app/loader.html",
            "workspace123");
  }

  @Test
  public void shouldBuildJwtProxyConfigInYamlFormat() throws Exception {
    // given
    Set<String> excludes = new HashSet<>();
    jwtProxyConfigBuilder.addVerifierProxy(
        8080, "http://tomcat:8080", new HashSet<>(excludes), false, "", "/there");
    excludes.add("/api/liveness");
    excludes.add("/other/exclude");
    jwtProxyConfigBuilder.addVerifierProxy(
        4101, "ws://terminal:4101", new HashSet<>(excludes), true, "/cookies", "/here");

    // when
    String jwtProxyConfigYaml = jwtProxyConfigBuilder.build();
    // then
    assertEquals(
        jwtProxyConfigYaml,
        Files.readFile(getClass().getClassLoader().getResourceAsStream("jwtproxy-confg.yaml")));
  }
}
