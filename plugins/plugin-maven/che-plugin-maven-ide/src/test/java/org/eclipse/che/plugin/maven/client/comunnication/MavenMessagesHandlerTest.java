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
package org.eclipse.che.plugin.maven.client.comunnication;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.console.CommandConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.plugin.maven.client.MavenJsonRpcHandler;
import org.eclipse.che.plugin.maven.client.comunnication.progressor.background.BackgroundLoaderPresenter;
import org.eclipse.che.plugin.maven.shared.dto.PercentMessageDto;
import org.eclipse.che.plugin.maven.shared.dto.ProjectsUpdateMessage;
import org.eclipse.che.plugin.maven.shared.dto.TextMessageDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MavenMessagesHandlerTest {
  @Mock private EventBus eventBus;
  @Mock private BackgroundLoaderPresenter dependencyResolver;
  @Mock private PomEditorReconciler pomEditorReconciler;
  @Mock private ProcessesPanelPresenter processesPanelPresenter;
  @Mock private CommandConsoleFactory commandConsoleFactory;
  @Mock private MavenJsonRpcHandler mavenJsonRpcHandler;
  @Mock private AppContext appContext;

  @Mock private ProjectsUpdateMessage projectsUpdateMessage;
  @Mock private Promise<Optional<Container>> optionalContainer;
  @Mock private Container rootContainer;

  private MavenMessagesHandler mavenMessagesHandler;

  @Before
  public void setUp() throws Exception {
    when(appContext.getWorkspaceRoot()).thenReturn(rootContainer);

    mavenMessagesHandler =
        new MavenMessagesHandler(
            eventBus,
            mavenJsonRpcHandler,
            dependencyResolver,
            pomEditorReconciler,
            processesPanelPresenter,
            commandConsoleFactory,
            appContext);
  }

  @Test
  public void catchUpdateEventAndUpdateOnlyParentProjects() throws Exception {
    final List<String> updatedProjects =
        Arrays.asList(
            "/che/core",
            "/project",
            "/che/core/che-core-db-vendor-postgresql",
            "/che/samples/sample-plugin-json",
            "/che/wsmaster/che-core-api-auth",
            "/project/api",
            "/che/wsagent",
            "/che",
            "/che/plugins/plugin-github/che-plugin-github-pullrequest",
            "/che/plugins/plugin-java-debugger");

    when(projectsUpdateMessage.getUpdatedProjects()).thenReturn(updatedProjects);
    when(rootContainer.getContainer(anyString())).thenReturn(optionalContainer);

    mavenMessagesHandler.handleUpdate(projectsUpdateMessage);

    verify(pomEditorReconciler).reconcilePoms(updatedProjects);
    verify(rootContainer).getContainer(eq("/che"));
    verify(rootContainer).getContainer(eq("/project"));
  }

  @Test
  public void percentNotificationShouldBeHandled() throws Exception {
    PercentMessageDto percentMessageDto = Mockito.mock(PercentMessageDto.class);
    when(percentMessageDto.getPercent()).thenReturn(0.7);

    mavenMessagesHandler.handlePercentNotification(percentMessageDto);

    verify(dependencyResolver).updateProgressBar(70);
  }

  @Test
  public void textNotificationShouldBeUpdated() throws Exception {
    TextMessageDto textMessageDto = mock(TextMessageDto.class);
    String textMessage = "textMessage";
    when(textMessageDto.getText()).thenReturn(textMessage);

    mavenMessagesHandler.handleTextNotification(textMessageDto);

    verify(dependencyResolver).setProgressLabel(textMessage);
  }
}
