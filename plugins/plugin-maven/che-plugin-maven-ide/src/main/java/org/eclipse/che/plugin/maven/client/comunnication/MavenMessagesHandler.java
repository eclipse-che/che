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
package org.eclipse.che.plugin.maven.client.comunnication;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.plugin.maven.client.comunnication.progressor.background.BackgroundLoaderPresenter;
import org.eclipse.che.plugin.maven.shared.MessageType;
import org.eclipse.che.plugin.maven.shared.dto.ArchetypeOutput;
import org.eclipse.che.plugin.maven.shared.dto.NotificationMessage;
import org.eclipse.che.plugin.maven.shared.dto.ProjectsUpdateMessage;
import org.eclipse.che.plugin.maven.shared.dto.StartStopNotification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ARCHETYPE_CHANEL_NAME;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_CHANEL_NAME;

/**
 * Handler which receives messages from the maven server.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
@Singleton
public class MavenMessagesHandler {

    private final EventBus                  eventBus;
    private final DtoFactory                factory;
    private final BackgroundLoaderPresenter dependencyResolver;
    private final PomEditorReconciler       pomEditorReconciler;
    private final ProcessesPanelPresenter   processesPanelPresenter;
    private final CommandConsoleFactory     commandConsoleFactory;
    private final AppContext appContext;

    @Inject
    public MavenMessagesHandler(EventBus eventBus,
                                DtoFactory factory,
                                BackgroundLoaderPresenter dependencyResolver,
                                PomEditorReconciler pomEditorReconciler,
                                WsAgentStateController wsAgentStateController,
                                ProcessesPanelPresenter processesPanelPresenter,
                                CommandConsoleFactory commandConsoleFactory,
                                AppContext appContext) {
        this.eventBus = eventBus;
        this.factory = factory;

        this.dependencyResolver = dependencyResolver;
        this.pomEditorReconciler = pomEditorReconciler;
        this.processesPanelPresenter = processesPanelPresenter;
        this.commandConsoleFactory = commandConsoleFactory;
        this.appContext = appContext;

        handleOperations(factory, wsAgentStateController);
    }

    private void handleOperations(final DtoFactory factory, final WsAgentStateController wsAgentStateController) {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
                    @Override
                    public void apply(MessageBus messageBus) throws OperationException {
                        try {
                            handleMavenServerEvents(messageBus);
                            handleMavenArchetype(messageBus);
                        } catch (WebSocketException e) {
                            dependencyResolver.hide();
                            Log.error(getClass(), e);
                        }
                    }
                });
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
                dependencyResolver.hide();
            }
        });
    }


    private void handleMavenServerEvents(final MessageBus messageBus) throws WebSocketException {
        messageBus.subscribe(MAVEN_CHANEL_NAME, new MessageHandler() {
            @Override
            public void onMessage(String message) {
                Jso jso = Jso.deserialize(message);
                int type = jso.getFieldCastedToInteger("$type");
                MessageType messageType = MessageType.valueOf(type);
                switch (messageType) {
                    case NOTIFICATION:
                        NotificationMessage dto = factory.createDtoFromJson(message, NotificationMessage.class);
                        handleNotification(dto);
                        break;

                    case UPDATE:
                        handleUpdate(factory.createDtoFromJson(message, ProjectsUpdateMessage.class));
                        break;

                    case START_STOP:
                        handleStartStop(factory.createDtoFromJson(message, StartStopNotification.class));
                        break;

                    default:
                        Log.error(getClass(), "Unknown message type:" + messageType);
                }
            }
        });
    }

    private void handleMavenArchetype(final MessageBus messageBus)  {
        final DefaultOutputConsole outputConsole = (DefaultOutputConsole)commandConsoleFactory.create("Maven Archetype");

        try {
            messageBus.subscribe(MAVEN_ARCHETYPE_CHANEL_NAME, new MessageHandler() {
                @Override
                public void onMessage(String message) {
                    Log.info(getClass(), message);
                    ArchetypeOutput archetypeOutput = factory.createDtoFromJson(message, ArchetypeOutput.class);
                    processesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), outputConsole);
                    switch (archetypeOutput.getState()) {
                        case START:
                            outputConsole.clearOutputsButtonClicked();
                            outputConsole.printText(archetypeOutput.getOutput(),"green");
                            break;
                        case IN_PROGRESS:
                            outputConsole.printText(archetypeOutput.getOutput());
                            break;
                        case DONE:
                            outputConsole.printText(archetypeOutput.getOutput(),"green");
                            break;
                        case ERROR:
                            outputConsole.printText(archetypeOutput.getOutput(),"red");
                            break;
                        default:
                            break;
                    }
                }
            });
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    private void handleStartStop(StartStopNotification dto) {
        if (dto.isStart()) {
            dependencyResolver.show();
        } else {
            dependencyResolver.hide();
        }
    }

    private void handleUpdate(ProjectsUpdateMessage dto) {
        List<String> updatedProjects = dto.getUpdatedProjects();
        Set<String> projectToRefresh = computeUniqueHiLevelProjects(updatedProjects);
        for (final String path : projectToRefresh) {
            appContext.getWorkspaceRoot().getContainer(path).then(new Operation<Optional<Container>>() {
                @Override
                public void apply(Optional<Container> container) throws OperationException {
                    if (container.isPresent()) {
                        container.get().synchronize();
                    }
                }
            });
        }

        pomEditorReconciler.reconcilePoms(updatedProjects);
    }

    private Set<String> computeUniqueHiLevelProjects(List<String> updatedProjects) {
        Set<String> result = new HashSet<>();
        for (String project : updatedProjects) {
            Path path = new Path(project);
            if (path.segmentCount() > 1) {
                //TODO maven modules may exists in sub sub directory
                path = path.removeLastSegments(1);
            }
            result.add(path.toString());
        }
        return result;
    }

    private void handleNotification(NotificationMessage message) {
        if (message.getPercent() != 0) {
            dependencyResolver.updateProgressBar((int)(message.getPercent() * 100));
        } else {
            dependencyResolver.setProgressLabel(message.getText());
        }
    }
}
