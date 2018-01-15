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
package org.eclipse.che.api.languageserver.registry;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import javax.inject.Provider;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Anatoliy Bazko */
@Listeners(MockitoTestNGListener.class)
public class LanguageServerRegistryImplTest {

  private static final String PREFIX = "file://";
  private static final String FILE_PATH = "/projects/1/test.txt";
  private static final String PROJECT_PATH = "file:///projects/1";

  @Mock private ServerInitializer initializer;
  @Mock private LanguageServerLauncher languageServerLauncher;
  @Mock private LanguageDescription languageDescription;
  @Mock private LanguageServer languageServer;
  @Mock private Provider<ProjectManager> pmp;
  @Mock private ProjectManager pm;
  @Mock private CheLanguageClientFactory clientFactory;
  @Mock private CheLanguageClient languageClient;
  @Mock private HttpJsonRequestFactory httpJsonRequestFactory;
  @Mock private HttpJsonRequest httpJsonRequest;
  @Mock private HttpJsonResponse httpJsonResponse;
  @Mock private Workspace workspace;
  @Mock private LanguageRecognizer languageRecognizer;

  private LanguageServerRegistryImpl registry;
  private LanguageServerDescription serverDescription;
  private InitializeResult initializeResult;
  private CompletableFuture<InitializeResult> completableFuture;
  private ServerCapabilities serverCapabilities;

  @BeforeMethod
  public void setUp() throws Exception {

    this.serverCapabilities = new ServerCapabilities();
    serverDescription =
        new LanguageServerDescription(
            "foo", Collections.singletonList("id"), Collections.emptyList());
    initializeResult = new InitializeResult(serverCapabilities);

    completableFuture = completedFuture(initializeResult);

    when(languageServerLauncher.isAbleToLaunch()).thenReturn(true);
    when(languageServerLauncher.getDescription()).thenReturn(serverDescription);
    when(languageServerLauncher.isLocal()).thenReturn(true);
    when(languageDescription.getLanguageId()).thenReturn("id");
    when(languageDescription.getFileExtensions()).thenReturn(Collections.singletonList("txt"));
    when(languageDescription.getMimeType()).thenReturn("plain/text");

    when(languageServer.getTextDocumentService()).thenReturn(mock(TextDocumentService.class));
    when(languageServer.initialize(any(InitializeParams.class))).thenReturn(completableFuture);

    when(languageRecognizer.recognizeByPath(anyString())).thenReturn(languageDescription);
    when(languageRecognizer.recognizeById(anyString())).thenReturn(languageDescription);

    when(pmp.get()).thenReturn(pm);

    when(clientFactory.create(anyString())).thenReturn(languageClient);

    when(httpJsonRequestFactory.fromUrl(any(String.class))).thenReturn(httpJsonRequest);
    when(httpJsonRequest.useGetMethod()).thenReturn(httpJsonRequest);
    when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
    when(httpJsonResponse.asDto(any())).thenReturn(workspace);

    registry =
        spy(
            new LanguageServerRegistryImpl(
                "",
                "",
                httpJsonRequestFactory,
                new HashSet<>(),
                Collections.singleton(languageServerLauncher),
                Collections.singleton(languageDescription),
                pmp,
                initializer,
                null,
                clientFactory,
                languageRecognizer) {
              @Override
              protected String extractProjectPath(String filePath) throws LanguageServerException {
                return PROJECT_PATH;
              }
            });

    when(initializer.initialize(
            any(LanguageServerLauncher.class), any(LanguageClient.class), anyString()))
        .thenAnswer(invocation -> completedFuture(Pair.of(languageServer, initializeResult)));
  }

  @Test
  public void testFindServer() throws Exception {
    ServerCapabilities cap = registry.initialize(PREFIX + FILE_PATH);

    assertNotNull(cap);
    assertEquals(cap, serverCapabilities);
    Mockito.verify(initializer)
        .initialize(eq(languageServerLauncher), any(LanguageClient.class), eq(PROJECT_PATH));
  }
}
