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

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
@Listeners(MockitoTestNGListener.class)
public class ServerInitializerImplTest {

    @Mock
    private ServerInitializerObserver           observer;
    @Mock
    private LanguageDescription                 languageDescription;
    @Mock
    private LanguageServerLauncher              launcher;
    @Mock
    private LanguageServer                      server;
    @Mock
    private CompletableFuture<InitializeResult> completableFuture;
    @Mock
    private EventService                        eventService;

    private ServerInitializerImpl               initializer;

    @BeforeMethod
    public void setUp() throws Exception {
        initializer = spy(new ServerInitializerImpl(eventService));
    }

    @Test
    public void initializerShouldNotifyObservers() throws Exception {
        when(languageDescription.getLanguageId()).thenReturn("languageId");
        when(server.initialize(any(InitializeParams.class))).thenReturn(completableFuture);
        when(completableFuture.get()).thenReturn(mock(InitializeResult.class));

        when(launcher.getLanguageId()).thenReturn("languageId");
        when(launcher.launch(anyString(), any())).thenReturn(server);
        doNothing().when(initializer).registerCallbacks(server, launcher);

        initializer.addObserver(observer);
        LanguageServer languageServer = initializer.initialize(languageDescription, launcher, "/path");

        assertEquals(server, languageServer);
        verify(observer).onServerInitialized(eq(server), any(ServerCapabilities.class), eq(languageDescription), eq("/path"));
    }
}
