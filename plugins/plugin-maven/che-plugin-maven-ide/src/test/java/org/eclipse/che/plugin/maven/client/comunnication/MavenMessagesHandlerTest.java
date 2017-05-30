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
package org.eclipse.che.plugin.maven.client.comunnication;

import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.console.CommandConsoleFactory;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.plugin.maven.client.comunnication.progressor.background.BackgroundLoaderPresenter;
import org.eclipse.che.plugin.maven.shared.dto.ProjectsUpdateMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_CHANEL_NAME;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MavenMessagesHandlerTest {
    private static final String JSON_MESSAGE = "{\n" +
                                               "  \"deletedProjects\": [],\n" +
                                               "  \"updatedProjects\": [\n" +
                                               "    \"/che/core\",\n" +
                                               "    \"/project\",\n" +
                                               "    \"/che/core/che-core-db-vendor-postgresql\",\n" +
                                               "    \"/che/plugins/plugin-svn\",\n" +
                                               "    \"/che/samples/sample-plugin-json\",\n" +
                                               "    \"/che/wsmaster/che-core-api-auth\",\n" +
                                               "    \"/project/api\",\n" +
                                               "    \"/che/wsagent\",\n" +
                                               "    \"/che\",\n" +
                                               "    \"/che/plugins/plugin-github/che-plugin-github-pullrequest\",\n" +
                                               "    \"/che/plugins/plugin-java-debugger\"\n" +
                                               "  ],\n" +
                                               "  \"$type\": 2\n" +
                                               "}";
    @Mock
    private EventBus                  eventBus;
    @Mock
    private DtoFactory                factory;
    @Mock
    private BackgroundLoaderPresenter dependencyResolver;
    @Mock
    private PomEditorReconciler       pomEditorReconciler;
    @Mock
    private WsAgentStateController    wsAgentStateController;
    @Mock
    private ProcessesPanelPresenter   processesPanelPresenter;
    @Mock
    private CommandConsoleFactory     commandConsoleFactory;
    @Mock
    private AppContext                appContext;

    @Mock
    private WsAgentStateEvent                     wsAgentStateEvent;
    @Mock
    private ProjectsUpdateMessage                 projectsUpdateMessage;
    @Mock
    private Promise<MessageBus>                   messageBusPromise;
    @Mock
    private Promise<Optional<Container>>          optionalContainer;
    @Mock
    private MessageBus                            messageBus;
    @Mock
    private Container                             rootContainer;
    @Captor
    private ArgumentCaptor<WsAgentStateHandler>   wsAgentStateHandlerArgumentCaptor;
    @Captor
    private ArgumentCaptor<Operation<MessageBus>> operationArgumentCaptor;
    @Captor
    private ArgumentCaptor<MessageHandler>        messageHandlerArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        when(wsAgentStateController.getMessageBus()).thenReturn(messageBusPromise);
        when(appContext.getWorkspaceRoot()).thenReturn(rootContainer);

        new MavenMessagesHandler(eventBus,
                                 factory,
                                 dependencyResolver,
                                 pomEditorReconciler,
                                 wsAgentStateController,
                                 processesPanelPresenter,
                                 commandConsoleFactory,
                                 appContext);


    }

    @Test
    public void catchUpdateEventAndUpdateOnlyParentProjects() throws Exception {
        final List<String> updatedProjects = Arrays.asList("/che/core",
                                                           "/project",
                                                           "/che/core/che-core-db-vendor-postgresql",
                                                           "/che/plugins/plugin-svn",
                                                           "/che/samples/sample-plugin-json",
                                                           "/che/wsmaster/che-core-api-auth",
                                                           "/project/api",
                                                           "/che/wsagent",
                                                           "/che",
                                                           "/che/plugins/plugin-github/che-plugin-github-pullrequest",
                                                           "/che/plugins/plugin-java-debugger");

        when(factory.createDtoFromJson(eq(JSON_MESSAGE), eq(ProjectsUpdateMessage.class))).thenReturn(projectsUpdateMessage);
        when(projectsUpdateMessage.getUpdatedProjects()).thenReturn(updatedProjects);
        when(rootContainer.getContainer(anyString())).thenReturn(optionalContainer);

        verify(eventBus).addHandler(eq(WsAgentStateEvent.TYPE), wsAgentStateHandlerArgumentCaptor.capture());
        wsAgentStateHandlerArgumentCaptor.getValue().onWsAgentStarted(wsAgentStateEvent);

        verify(messageBusPromise).then(operationArgumentCaptor.capture());
        operationArgumentCaptor.getValue().apply(messageBus);

        verify(messageBus).subscribe(eq(MAVEN_CHANEL_NAME), messageHandlerArgumentCaptor.capture());
        messageHandlerArgumentCaptor.getValue().onMessage(JSON_MESSAGE);

        verify(factory).createDtoFromJson(JSON_MESSAGE, ProjectsUpdateMessage.class);
        verify(pomEditorReconciler).reconcilePoms(updatedProjects);
        verify(rootContainer).getContainer(eq("/che"));
        verify(rootContainer).getContainer(eq("/project"));
    }
}
