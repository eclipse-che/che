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
package org.eclipse.che.api.languageserver;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class FindServerTest {

  private static final String ID_1 = "id-1";
  private static final String WS_PATH = "path";

  @Mock private FindId findId;

  private RegistryContainer registryContainer;

  private FindServer findServer;

  @Mock private ServerCapabilities serverCapabilities;
  @Mock private LanguageServer languageServer;
  @Mock private TextDocumentService textDocumentService;
  @Mock private WorkspaceService workspaceService;

  @BeforeMethod
  public void setUp() {
    registryContainer = new RegistryContainer();
    findServer = new FindServer(registryContainer, findId);
  }

  @Test
  public void shouldGetProperExtendedLanguageServerById() {
    registryContainer.serverCapabilitiesRegistry.add(ID_1, serverCapabilities);
    registryContainer.languageServerRegistry.add(ID_1, languageServer);

    when(languageServer.getTextDocumentService()).thenReturn(textDocumentService);
    when(languageServer.getWorkspaceService()).thenReturn(workspaceService);

    ExtendedLanguageServer extendedLanguageServer = findServer.byId(ID_1);

    assertEquals(extendedLanguageServer.getCapabilities(), serverCapabilities);
    assertEquals(extendedLanguageServer.getId(), ID_1);
    assertEquals(extendedLanguageServer.getWorkspaceService(), workspaceService);
    assertEquals(extendedLanguageServer.getTextDocumentService(), textDocumentService);
  }

  @Test
  public void shouldGetNullWhenNoCapabilities() {
    registryContainer.languageServerRegistry.add(ID_1, languageServer);

    ExtendedLanguageServer extendedLanguageServer = findServer.byId(ID_1);

    assertNull(extendedLanguageServer);
  }

  @Test
  public void shouldGetNullWhenNoLanguageServer() {
    registryContainer.serverCapabilitiesRegistry.add(ID_1, serverCapabilities);

    ExtendedLanguageServer extendedLanguageServer = findServer.byId(ID_1);

    assertNull(extendedLanguageServer);
  }

  @Test
  public void shouldGetProperExtendedServerSetByPath() {
    when(findId.byPath(WS_PATH)).thenReturn(ImmutableSet.of(ID_1));

    registryContainer.serverCapabilitiesRegistry.add(ID_1, serverCapabilities);
    registryContainer.languageServerRegistry.add(ID_1, languageServer);

    when(languageServer.getTextDocumentService()).thenReturn(textDocumentService);
    when(languageServer.getWorkspaceService()).thenReturn(workspaceService);

    Set<ExtendedLanguageServer> extendedLanguageServers = findServer.byPath(WS_PATH);

    assertEquals(1, extendedLanguageServers.size());
    ExtendedLanguageServer extendedLanguageServer = extendedLanguageServers.iterator().next();

    assertEquals(extendedLanguageServer.getCapabilities(), serverCapabilities);
    assertEquals(extendedLanguageServer.getId(), ID_1);
    assertEquals(extendedLanguageServer.getWorkspaceService(), workspaceService);
    assertEquals(extendedLanguageServer.getTextDocumentService(), textDocumentService);
  }

  @Test
  public void shouldGetEmptyExtendedServerSetByPath() {
    when(findId.byPath(WS_PATH)).thenReturn(ImmutableSet.of());

    registryContainer.serverCapabilitiesRegistry.add(ID_1, serverCapabilities);
    registryContainer.languageServerRegistry.add(ID_1, languageServer);

    when(languageServer.getTextDocumentService()).thenReturn(textDocumentService);
    when(languageServer.getWorkspaceService()).thenReturn(workspaceService);

    Set<ExtendedLanguageServer> extendedLanguageServers = findServer.byPath(WS_PATH);

    assertEquals(0, extendedLanguageServers.size());
  }
}
