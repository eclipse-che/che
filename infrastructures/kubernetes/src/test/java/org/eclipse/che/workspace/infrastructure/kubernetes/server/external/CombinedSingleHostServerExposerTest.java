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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.REQUIRE_SUBDOMAIN;
import static org.mockito.Mockito.verify;

import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class CombinedSingleHostServerExposerTest {

  @Mock private KubernetesEnvironment env;

  private final String MACHINE = "machine";
  private final String SERVICE = "service";
  private final String SERVER = "server";
  private final ServicePort PORT = new ServicePort();

  @Mock private ExternalServerExposer<KubernetesEnvironment> subdomainExposer;
  @Mock private ExternalServerExposer<KubernetesEnvironment> subpathExposer;

  @Test
  public void shouldExposeDevfileServersOnSubdomans() {
    // given
    ServerConfig s1 = new ServerConfigImpl("1", "http", "/", emptyMap());
    ServerConfig s2 =
        new ServerConfigImpl("2", "http", "/", singletonMap(REQUIRE_SUBDOMAIN, "false"));
    ServerConfig s3 =
        new ServerConfigImpl("3", "http", "/", singletonMap(REQUIRE_SUBDOMAIN, "true"));

    CombinedSingleHostServerExposer<KubernetesEnvironment> serverExposer =
        new CombinedSingleHostServerExposer<>(subdomainExposer, subpathExposer);

    // when
    serverExposer.expose(env, MACHINE, SERVICE, SERVER, PORT, Map.of("s1", s1, "s2", s2, "s3", s3));

    // then
    verify(subdomainExposer).expose(env, MACHINE, SERVICE, SERVER, PORT, Map.of("s3", s3));
    verify(subpathExposer).expose(env, MACHINE, SERVICE, SERVER, PORT, Map.of("s1", s1, "s2", s2));
  }
}
