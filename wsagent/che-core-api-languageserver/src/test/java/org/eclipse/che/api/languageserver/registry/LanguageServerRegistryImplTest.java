/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.registry;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/** @author Anatoliy Bazko */
@Listeners(MockitoTestNGListener.class)
public class LanguageServerRegistryImplTest {

  //  private static final String PROJECTS_ROOT = "file:///projects";
  //  private static final String PREFIX = "file://";
  //  private static final String FILE_PATH = "/projects/1/test.txt";
  //  private static final String PROJECT_PATH = "file:///projects/1";
  //
  //  @Mock private ServerInitializer initializer;
  //  @Mock private LanguageServerLauncher languageServerLauncher;
  //  @Mock private LanguageDescription languageDescription;
  //  @Mock private LanguageServer languageServer;
  //  @Mock private Provider<ProjectManager_> pmp;
  //  @Mock private ProjectManager_ pm;
  //  @Mock private FolderEntry projectsRoot;
  //  @Mock private CheLanguageClientFactory clientFactory;
  //  @Mock private CheLanguageClient languageClient;
  //
  //  private LanguageServerRegistryImpl registry;
  //  private LanguageServerDescription serverDescription;
  //  private InitializeResult initializeResult;
  //  private CompletableFuture<InitializeResult> completableFuture;
  //  private ServerCapabilities serverCapabilities;
  //
  //  @BeforeMethod
  //  public void setUp() throws Exception {
  //    this.serverCapabilities = new ServerCapabilities();
  //    serverDescription =
  //        new LanguageServerDescription(
  //            "foo", Collections.singletonList("id"), Collections.emptyList());
  //    initializeResult = new InitializeResult(serverCapabilities);
  //
  //    completableFuture = CompletableFuture.completedFuture(initializeResult);
  //
  //    when(languageServerLauncher.isAbleToLaunch()).thenReturn(true);
  //    when(languageServerLauncher.getDescription()).thenReturn(serverDescription);
  //    when(languageDescription.getLanguageId()).thenReturn("id");
  //    when(languageDescription.getFileExtensions()).thenReturn(Collections.singletonList("txt"));
  //    when(languageDescription.getMimeType()).thenReturn("plain/text");
  //
  //    when(languageServer.getTextDocumentService()).thenReturn(mock(TextDocumentService.class));
  //    when(languageServer.initialize(any(InitializeParams.class))).thenReturn(completableFuture);
  //
  //    when(pmp.get()).thenReturn(pm);
  //    when(projectsRoot.getPath()).thenReturn(Path.of(PROJECTS_ROOT));
  //    when(pm.getProjectsRoot()).thenReturn(projectsRoot);
  //
  //    when(clientFactory.create(anyString())).thenReturn(languageClient);
  //
  //    registry =
  //        spy(
  //            new LanguageServerRegistryImpl(
  //                Collections.singleton(languageServerLauncher),
  //                Collections.singleton(languageDescription),
  //                pmp,
  //                initializer,
  //                null,
  //                clientFactory,
  //                pathResolver) {
  //              @Override
  //              protected String extractProjectPath(String filePath) throws LanguageServerException {
  //                return PROJECT_PATH;
  //              }
  //            });
  //
  //    when(initializer.initialize(
  //            any(LanguageServerLauncher.class), any(LanguageClient.class), anyString()))
  //        .thenAnswer(
  //            invocation -> {
  //              return CompletableFuture.completedFuture(Pair.of(languageServer, initializeResult));
  //            });
  //  }
  //
  //  @Test
  //  public void testFindServer() throws Exception {
  //    ServerCapabilities cap = registry.initialize(PREFIX + FILE_PATH);
  //
  //    assertNotNull(cap);
  //    assertEquals(cap, serverCapabilities);
  //    verify(initializer)
  //        .initialize(eq(languageServerLauncher), any(LanguageClient.class), eq(PROJECT_PATH));
  //  }
}
