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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
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
}
