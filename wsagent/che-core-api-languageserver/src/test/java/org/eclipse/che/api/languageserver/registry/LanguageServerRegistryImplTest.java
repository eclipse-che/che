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

import com.google.inject.Provider;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.commons.lang.Pair;
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

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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

    private static final String PROJECTS_ROOT = "file:///projects";
    private static final String PREFIX        = "file://";
    private static final String FILE_PATH     = "/projects/1/test.txt";
    private static final String PROJECT_PATH  = "file:///projects/1";

    @Mock
    private ServerInitializer        initializer;
    @Mock
    private LanguageServerLauncher   languageServerLauncher;
    @Mock
    private LanguageDescription      languageDescription;
    @Mock
    private LanguageServer           languageServer;
    @Mock
    private Provider<ProjectManager> pmp;
    @Mock
    private ProjectManager           pm;
    @Mock
    private FolderEntry              projectsRoot;

    private LanguageServerRegistryImpl          registry;
    private LanguageServerDescription           serverDescription;
    private InitializeResult                    initializeResult;
    private CompletableFuture<InitializeResult> completableFuture;
    private ServerCapabilities       serverCapabilities;

    @BeforeMethod
    public void setUp() throws Exception {
        this.serverCapabilities= new ServerCapabilities();
        serverDescription = new LanguageServerDescription("foo", Collections.singletonList("id"), Collections.emptyList());
        initializeResult = new InitializeResult(serverCapabilities);

        completableFuture = CompletableFuture.completedFuture(initializeResult);

        when(languageServerLauncher.isAbleToLaunch()).thenReturn(true);
        when(languageServerLauncher.getDescription()).thenReturn(serverDescription);
        when(languageDescription.getLanguageId()).thenReturn("id");
        when(languageDescription.getFileExtensions()).thenReturn(Collections.singletonList("txt"));
        when(languageDescription.getMimeType()).thenReturn("plain/text");

        when(languageServer.getTextDocumentService()).thenReturn(mock(TextDocumentService.class));
        when(languageServer.initialize(any(InitializeParams.class))).thenReturn(completableFuture);

        when(pmp.get()).thenReturn(pm);
        when(projectsRoot.getPath()).thenReturn(Path.of(PROJECTS_ROOT));
        when(pm.getProjectsRoot()).thenReturn(projectsRoot);

        registry = spy(new LanguageServerRegistryImpl(Collections.singleton(languageServerLauncher),
                        Collections.singleton(languageDescription), pmp, initializer) {
            @Override
            protected String extractProjectPath(String filePath) throws LanguageServerException {
                return PROJECT_PATH;
            }
        });

        when(initializer.initialize(any(LanguageServerLauncher.class), anyString())).thenAnswer(invocation -> {
            return CompletableFuture.completedFuture(Pair.of(languageServer, initializeResult));
        });
    }

    @Test
    public void testFindServer() throws Exception {
        ServerCapabilities cap = registry.initialize(PREFIX + FILE_PATH);

        assertNotNull(cap);
        assertEquals(cap, serverCapabilities);
        verify(initializer).initialize(eq(languageServerLauncher), eq(PROJECT_PATH));
    }
}
