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

package org.eclipse.che.api.languageserver;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.messager.ShowMessageJsonRpcTransmitter;
import org.eclipse.che.api.languageserver.registry.CheLanguageClient;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.registry.ServerInitializerImpl;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 *
 */
@Listeners(MockitoTestNGListener.class)
public class ShowMessageRequestTest {

    private LocalTestLSLauncher launcher;

    @Mock
    private EventService eventService;

    @Mock
    private ShowMessageJsonRpcTransmitter transmitter;
    private LanguageServer                server;

    @BeforeMethod
    public void setUp() throws Exception {
        List<String> command = new ArrayList<>();
        command.add(ShowMessageRequestTest.class.getResource("/ls").getFile() + "/test-ls/server.sh");
        launcher =
                new LocalTestLSLauncher(command,
                                        new LanguageServerDescription("foo", Collections.singletonList("aaa"), Collections.emptyList()));

    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (server != null) {
            server.shutdown();
            server.exit();
        }
    }

    @Test
    public void testName() throws Exception {
        CompletableFuture<MessageActionItem> future = new CompletableFuture<>();
        when(transmitter.sendShowMessageRequest(any())).thenReturn(future);

        ServerInitializerImpl initializer = new ServerInitializerImpl();
        CheLanguageClient client = new CheLanguageClient(eventService, transmitter, "id");
        CompletableFuture<Pair<LanguageServer, InitializeResult>> initialize = initializer.initialize(launcher, client, "/tmp");
        Pair<LanguageServer, InitializeResult> resultPair = initialize.get();
        server = resultPair.first;
        Thread.sleep(1200);
        ArgumentCaptor<ShowMessageRequestParams> captor = ArgumentCaptor.forClass(ShowMessageRequestParams.class);
        verify(transmitter).sendShowMessageRequest(captor.capture());

        ShowMessageRequestParams value = captor.getValue();
        assertNotNull(value);
        assertEquals(value.getType(), MessageType.Error);
        assertEquals(value.getMessage(), "Error Message!!!!");
    }

}
