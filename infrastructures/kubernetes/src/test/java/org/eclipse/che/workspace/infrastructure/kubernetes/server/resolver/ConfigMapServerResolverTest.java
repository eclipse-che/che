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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class ConfigMapServerResolverTest {
  @Mock private ServerResolver nativeServerResolver;

  @Test
  public void shouldIncludeServersFromNativeResolver() {
    // given
    ServerImpl server = new ServerImpl("server", ServerStatus.UNKNOWN, emptyMap());
    when(nativeServerResolver.resolveExternalServers("test"))
        .thenReturn(singletonMap("s1", server));

    ConfigMapServerResolver serverResolver =
        new ConfigMapServerResolver(emptyList(), emptyList(), "che.host", nativeServerResolver);

    // when
    Map<String, ServerImpl> resolvedServers = serverResolver.resolve("test");

    // then
    assertTrue(resolvedServers.containsKey("s1"));
    assertEquals(resolvedServers.get("s1"), server);
  }

  @Test
  public void shouldSetEndpointOrigin() {
    // given
    ConfigMap serverConfigMap =
        new ConfigMapBuilder()
            .withNewMetadata()
            .addToAnnotations(
                Annotations.newSerializer()
                    .machineName("m1")
                    .server(
                        "svr",
                        new ServerConfigImpl()
                            .withPort("8080")
                            .withProtocol("http")
                            .withPath("/kachny")
                            .withAttributes(
                                ImmutableMap.of(ServerConfig.ENDPOINT_ORIGIN, "/kachny")))
                    .annotations())
            .endMetadata()
            .build();

    ConfigMapServerResolver serverResolver =
        new ConfigMapServerResolver(
            emptyList(), singletonList(serverConfigMap), "che.host", nativeServerResolver);

    // when
    Map<String, ServerImpl> resolvedServers = serverResolver.resolve("m1");

    // then
    ServerImpl svr = resolvedServers.get("svr");
    assertNotNull(svr);

    assertEquals("/kachny", ServerConfig.getEndpointOrigin(svr.getAttributes()));
    assertEquals("http://che.host/kachny/kachny/", svr.getUrl());
  }
}
