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
package org.eclipse.che.api.workspace.server;

import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_ENVIRONMENT_STATUS_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_IDE_URL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests {@link org.eclipse.che.api.workspace.server.WorkspaceLinksGenerator}. */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceLinksGeneratorTest {
  private static final String URI_BASE = "http://localhost/api/workspace";

  @Mock private ServiceContext serviceContextMock;

  @Mock private WorkspaceRuntimes runtimes;

  @Mock private WorkspaceImpl workspace;

  @Mock private RuntimeContext runtimeCtx;

  @Mock private PreviewUrlLinksVariableGenerator previewUrlGenerator;

  private Map<String, String> expectedStoppedLinks;
  private Map<String, String> expectedRunningLinks;
  private WorkspaceLinksGenerator linksGenerator;

  @BeforeMethod
  public void setUp() throws Exception {
    when(workspace.getId()).thenReturn("wside-123877234580");
    when(workspace.getNamespace()).thenReturn("my-namespace");
    when(workspace.getName()).thenReturn("my-name");

    when(runtimeCtx.getOutputChannel()).thenReturn(URI.create("ws://localhost/output/websocket"));
    when(runtimes.getRuntimeContext(workspace.getId())).thenReturn(Optional.of(runtimeCtx));

    final UriBuilder uriBuilder = new UriBuilderImpl();
    uriBuilder.uri(URI_BASE);
    lenient().when(serviceContextMock.getServiceUriBuilder()).thenReturn(uriBuilder);
    lenient().when(serviceContextMock.getBaseUriBuilder()).thenReturn(uriBuilder);

    linksGenerator = new WorkspaceLinksGenerator(runtimes, previewUrlGenerator, "ws://localhost");

    expectedStoppedLinks = new HashMap<>();
    expectedStoppedLinks.put(LINK_REL_SELF, "http://localhost/api/workspace/wside-123877234580");
    expectedStoppedLinks.put(LINK_REL_IDE_URL, "http://localhost/my-namespace/my-name");

    expectedRunningLinks = new HashMap<>(expectedStoppedLinks);
    expectedRunningLinks.put(LINK_REL_ENVIRONMENT_STATUS_CHANNEL, "ws://localhost");
    expectedRunningLinks.put(
        LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL, "ws://localhost/output/websocket");
  }

  @Test
  public void genOfRunningWorkspaceLinks() throws Exception {
    when(workspace.getStatus()).thenReturn(WorkspaceStatus.RUNNING);

    assertEquals(linksGenerator.genLinks(workspace, serviceContextMock), expectedRunningLinks);
  }

  @Test
  public void genOfStoppedWorkspaceLinks() throws Exception {
    when(workspace.getStatus()).thenReturn(WorkspaceStatus.STOPPED);

    assertEquals(linksGenerator.genLinks(workspace, serviceContextMock), expectedStoppedLinks);
  }

  @Test
  public void genOfDifferentUrl() throws Exception {
    // given
    UriBuilder uriBuilder = new UriBuilderImpl();
    uriBuilder.uri("https://mydomain:7345/api/workspace");
    doReturn(uriBuilder).when(serviceContextMock).getServiceUriBuilder();

    linksGenerator = new WorkspaceLinksGenerator(runtimes, previewUrlGenerator, "ws://localhost");
    // when
    Map<String, String> actual = linksGenerator.genLinks(workspace, serviceContextMock);
    // then
    expectedRunningLinks =
        ImmutableMap.of(
            LINK_REL_SELF,
            "https://mydomain:7345/api/workspace/wside-123877234580",
            LINK_REL_IDE_URL,
            "https://mydomain:7345/my-namespace/my-name",
            LINK_REL_ENVIRONMENT_STATUS_CHANNEL,
            "wss://mydomain:7345",
            LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL,
            "ws://localhost/output/websocket");
    assertEquals(actual, expectedRunningLinks);
  }
}
