/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.server.registry;

import io.typefox.lsapi.InitializeParams;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.services.LanguageServer;
import io.typefox.lsapi.services.TextDocumentService;
import io.typefox.lsapi.services.WindowService;

import org.eclipse.che.plugin.languageserver.server.factory.JsonLanguageServerFactory;
import org.eclipse.che.plugin.languageserver.server.factory.LanguageServerFactory;
import org.eclipse.che.plugin.languageserver.server.messager.PublishDiagnosticsParamsMessenger;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Anatoliy Bazko
 */
@Listeners(MockitoTestNGListener.class)
public class LanguageServerRegistryImplTest {

    private static final String PREFIX = "file://";
    private static final String PATH1  = "/projects/1/test.json";
    private static final String PATH2  = "/projects/2/test.json";

    @Mock
    private ServerInitializer                 initializer;
    @Mock
    private LanguageServerFactory             languageServerFactory;
    @Mock
    private LanguageDescription               languageDescription;
    @Mock
    private LanguageServer                    languageServer1;
    @Mock
    private LanguageServer                    languageServer2;
    @Mock
    private PublishDiagnosticsParamsMessenger publishDiagnosticsMessenger;
    @Mock
    private Set<LanguageServerFactory>        factories;

    private LanguageServerRegistryImpl registry;

    @BeforeMethod
    public void setUp() throws Exception {
        when(initializer.initialize(any(LanguageServerFactory.class), anyString())).thenReturn(languageServer1).thenReturn(languageServer2);
        when(languageServerFactory.getLanguageDescription()).thenReturn(languageDescription);
        when(languageDescription.getLanguageId()).thenReturn(JsonLanguageServerFactory.LANGUAGE_ID);
        when(languageDescription.getFileExtensions()).thenReturn(asList(JsonLanguageServerFactory.EXTENSIONS));
        when(languageDescription.getMimeTypes()).thenReturn(asList(JsonLanguageServerFactory.MIME_TYPES));

        CompletableFuture completableFuture = mock(CompletableFuture.class);
        when(completableFuture.get()).thenReturn(mock(InitializeResult.class));

        when(languageServer1.getTextDocumentService()).thenReturn(mock(TextDocumentService.class));
        when(languageServer1.getWindowService()).thenReturn(mock(WindowService.class));
        when(languageServer1.initialize(any(InitializeParams.class))).thenReturn(completableFuture);

        when(languageServer2.getTextDocumentService()).thenReturn(mock(TextDocumentService.class));
        when(languageServer2.getWindowService()).thenReturn(mock(WindowService.class));
        when(languageServer2.initialize(any(InitializeParams.class))).thenReturn(completableFuture);


        registry = spy(new LanguageServerRegistryImpl(Collections.singleton(languageServerFactory),
                                                      null,
                                                      initializer));
        doReturn("/1").when(registry).findProject(PATH1);
        doReturn("/2").when(registry).findProject(PATH2);
    }

    @Test
    public void findServerShouldReturnSameJsonServerFor() throws Exception {
        LanguageServer server = registry.findServer(PREFIX + PATH1);

        assertNotNull(server);
        assertEquals(server, languageServer1);

        server = registry.findServer(PREFIX + PATH1);

        assertNotNull(server);
        assertEquals(server, languageServer1);

        server = registry.findServer(PREFIX + PATH2);

        assertNotNull(server);
        assertEquals(server, languageServer1);
    }
}
