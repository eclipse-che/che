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

import io.typefox.lsapi.InitializeParams;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.services.LanguageServer;
import io.typefox.lsapi.services.TextDocumentService;
import io.typefox.lsapi.services.WindowService;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.languageserver.shared.model.impl.LanguageDescriptionImpl;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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

    private static final String PREFIX = "file://";
    private static final String FILE_PATH = "/projects/1/test.txt";
    private static final String PROJECT_PATH = "/1";

    @Mock
    private ServerInitializer initializer;
    @Mock
    private LanguageServerLauncher languageServerLauncher;
    @Mock
    private LanguageDescription languageDescription;
    @Mock
    private LanguageServer languageServer;
    @Mock
    private InitializeResult initializeResult;
    @Mock
    private ServerCapabilities serverCapabilities;
    @Mock
    private CompletableFuture<InitializeResult> completableFuture;

    @BeforeMethod
    public void setUp() throws Exception {
        when(completableFuture.get()).thenReturn(initializeResult);
        when(initializeResult.getCapabilities()).thenReturn(serverCapabilities);

        when(languageServerLauncher.getLanguageDescription()).thenReturn(languageDescription);
        when(languageServerLauncher.isAbleToLaunch()).thenReturn(true);
        when(languageDescription.getLanguageId()).thenReturn("id");
        when(languageDescription.getFileExtensions()).thenReturn(Collections.singletonList("txt"));
        when(languageDescription.getMimeTypes()).thenReturn(Collections.singletonList("plain/text"));

        when(languageServer.getTextDocumentService()).thenReturn(mock(TextDocumentService.class));
        when(languageServer.getWindowService()).thenReturn(mock(WindowService.class));
        when(languageServer.initialize(any(InitializeParams.class))).thenReturn(completableFuture);

        when(initializer.initialize(any(LanguageServerLauncher.class), anyString())).thenAnswer(invocation -> {
            return languageServer;
        });
    }
    
    private LanguageServerLauncher createLauncher(String id, List<String> extensions, List<String> patterns, ServerCapabilities capabilities) throws LanguageServerException {
        LanguageDescriptionImpl description = new LanguageDescriptionImpl();
        description.setFileExtensions(extensions);
        description.setFilenamePatterns(patterns);
        description.setLanguageId(id);
        LanguageServerLauncher launcher = mock(LanguageServerLauncher.class);
        when(launcher.getLanguageDescription()).thenReturn(description);
        when(launcher.isAbleToLaunch()).thenReturn(true);
        return launcher;
    }

    @Test
    public void testFindServer() throws Exception {
        LanguageServerRegistryImpl registry = spy(
                        new LanguageServerRegistryImpl(Collections.singleton(languageServerLauncher), null, initializer));
        doReturn(PROJECT_PATH).when(registry).extractProjectPath(FILE_PATH);

        LanguageServer server = registry.findServer(PREFIX + FILE_PATH);

        assertNotNull(server);
        assertEquals(server, languageServer);
        verify(initializer).initialize(eq(languageServerLauncher), eq(PROJECT_PATH));
    }
    
    @Test
    void testFindByPattern() throws Exception {
        LanguageServerLauncher xmlLauncher = createLauncher("xml", Arrays.asList("xml"), null, null);
        LanguageServerLauncher pomLauncher = createLauncher("xml", Arrays.asList(), Arrays.asList("pom\\.xml"), null);
        LanguageServer xmlServer = mock(LanguageServer.class);
        LanguageServer pomServer = mock(LanguageServer.class);
        ServerInitializer initializer= mock(ServerInitializer.class);
        when(initializer.initialize(any(LanguageServerLauncher.class), anyString())).thenAnswer(new Answer<LanguageServer>() {

            @Override
            public LanguageServer answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getArguments()[0] == xmlLauncher) {
                    return xmlServer; 
                } else if (invocation.getArguments()[0] == pomLauncher) {
                    return pomServer;
                }
                return null;
            }
        });
        LanguageServerRegistryImpl registry = spy(
                        new LanguageServerRegistryImpl(new HashSet<>(Arrays.asList(xmlLauncher, pomLauncher)), null, initializer) {
                            @Override
                            protected String extractProjectPath(String filePath) throws LanguageServerException {
                                return PROJECT_PATH;
                            }
                        });
        
        
        assertEquals(xmlServer, registry.findServer("/foo/bar/foo.xml"));
        assertEquals(pomServer, registry.findServer("/foo/bar/pom.xml"));
    }
}
