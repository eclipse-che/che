/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static org.testng.Assert.assertEquals;

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
    jwtProxyConfigBuilder = new JwtProxyConfigBuilder("workspace123");
  }

  @Test
  public void shouldBuildJwtProxyConfigInYamlFormat() throws Exception {
    // given
    Set<String> excludes = new HashSet<>();
    jwtProxyConfigBuilder.addVerifierProxy(8080, "http://tomcat:8080", new HashSet<>(excludes));
    excludes.add("/api/liveness");
    excludes.add("/other/exclude");
    jwtProxyConfigBuilder.addVerifierProxy(4101, "ws://terminal:4101", new HashSet<>(excludes));

    // when
    String jwtProxyConfigYaml = jwtProxyConfigBuilder.build();

    // then
    assertEquals(
        jwtProxyConfigYaml,
        Files.readFile(getClass().getClassLoader().getResourceAsStream("jwtproxy-confg.yaml")));
  }
}
