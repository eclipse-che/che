/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver.registry;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Anatoliy Bazko
 */
@Listeners(MockitoTestNGListener.class)
public class LanguageServerRegistryImplTest {

    private static final String PREFIX       = "file://";
    private static final String FILE_PATH    = "/projects/1/test.txt";
    private static final String PROJECT_PATH = "/1";

    @Mock
    private ServerInitializer                   initializer;
    @Mock
    private LanguageServerLauncher              languageServerLauncher;
    @Mock
    private LanguageDescription                 languageDescription;
    @Mock
    private LanguageServer                      languageServer;
    @Mock
    private InitializeResult                    initializeResult;
    @Mock
    private ServerCapabilities                  serverCapabilities;
    @Mock
    private CompletableFuture<InitializeResult> completableFuture;

    private LanguageServerRegistryImpl registry;

    @BeforeMethod
    public void setUp() throws Exception {
        when(completableFuture.get()).thenReturn(initializeResult);
        when(initializeResult.getCapabilities()).thenReturn(serverCapabilities);

        when(languageServerLauncher.getLanguageId()).thenReturn("id");
        when(languageServerLauncher.isAbleToLaunch()).thenReturn(true);
        when(languageDescription.getLanguageId()).thenReturn("id");
        when(languageDescription.getFileExtensions()).thenReturn(Collections.singletonList("txt"));
        when(languageDescription.getMimeType()).thenReturn("plain/text");

        when(languageServer.getTextDocumentService()).thenReturn(mock(TextDocumentService.class));
        when(languageServer.initialize(any(InitializeParams.class))).thenReturn(completableFuture);

        registry = spy(new LanguageServerRegistryImpl(Collections.singleton(languageServerLauncher),
                        Collections.singleton(languageDescription), null, initializer));

        when(initializer.initialize(any(LanguageDescription.class), any(LanguageServerLauncher.class), anyString()))
                        .thenAnswer(invocation -> {
                            Object[] arguments = invocation.getArguments();
                            registry.onServerInitialized(languageServer, serverCapabilities, languageDescription, (String) arguments[2]);
                            return languageServer;
                        });

        doReturn(PROJECT_PATH).when(registry).extractProjectPath(FILE_PATH);
    }

    private LanguageServerLauncher createLauncher(String id, ServerCapabilities capabilities) throws LanguageServerException {
        LanguageServerLauncher launcher = mock(LanguageServerLauncher.class);
        when(launcher.getLanguageId()).thenReturn(id);
        when(launcher.isAbleToLaunch()).thenReturn(true);
        return launcher;
    }

    private LanguageDescription createDescription(String id, List<String> extensions, List<String> names) {
        LanguageDescription description = new LanguageDescription();
        description.setFileExtensions(extensions);
        description.setFileNames(names);
        description.setLanguageId(id);
        return description;
    }

    @Test
    public void testFindServer() throws Exception {
        LanguageServer server = registry.findServer(PREFIX + FILE_PATH);

        assertNotNull(server);
        assertEquals(server, languageServer);
        verify(initializer).initialize(eq(languageDescription), eq(languageServerLauncher), eq(PROJECT_PATH));
        verify(registry).onServerInitialized(eq(languageServer), eq(serverCapabilities), eq(languageDescription), eq(PROJECT_PATH));
    }

    @Test
    void testFindByPattern() throws Exception {
        LanguageServerLauncher xmlLauncher = createLauncher("xml", null);
        LanguageServerLauncher pomLauncher = createLauncher("pom", null);
        LanguageDescription xmlDesc = createDescription("xml", Arrays.asList("xml"), null);
        LanguageDescription pomDesc = createDescription("pom", Arrays.asList(), Arrays.asList("pom.xml"));
        LanguageServer xmlServer = mock(LanguageServer.class);
        LanguageServer pomServer = mock(LanguageServer.class);
        ServerInitializer initializer = mock(ServerInitializer.class);
        when(initializer.initialize(eq(pomDesc), any(LanguageServerLauncher.class), anyString())).thenReturn(pomServer);
        when(initializer.initialize(eq(xmlDesc), any(LanguageServerLauncher.class), anyString())).thenReturn(xmlServer);
        LanguageServerRegistryImpl registry = spy(new LanguageServerRegistryImpl(new HashSet<>(Arrays.asList(xmlLauncher, pomLauncher)),
                        new HashSet<>(Arrays.asList(xmlDesc, pomDesc)), null, initializer) {
            @Override
            protected String extractProjectPath(String filePath) throws LanguageServerException {
                return PROJECT_PATH;
            }
        });

        assertEquals(xmlServer, registry.findServer("/foo/bar/foo.xml"));
        assertEquals(pomServer, registry.findServer("/foo/bar/pom.xml"));
    }
}
