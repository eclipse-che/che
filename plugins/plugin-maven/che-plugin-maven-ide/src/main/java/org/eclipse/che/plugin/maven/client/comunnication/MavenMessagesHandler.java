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
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.plugin.maven.client.comunnication.progressor.background.BackgroundLoaderPresenter;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.eclipse.che.plugin.maven.shared.MessageType;
import org.eclipse.che.plugin.maven.shared.dto.NotificationMessage;
import org.eclipse.che.plugin.maven.shared.dto.ProjectsUpdateMessage;
import org.eclipse.che.plugin.maven.shared.dto.StartStopNotification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;

/**
 * Handler which receives messages from the maven server.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
@Singleton
public class MavenMessagesHandler {

    private final EventBus                  eventBus;
    private final NotificationManager       notificationManager;
    private final BackgroundLoaderPresenter dependencyResolver;
    private final PomEditorReconciler       pomEditorReconciler;
    private final AppContext                appContext;

    @Inject
    public MavenMessagesHandler(EventBus eventBus,
                                NotificationManager notificationManager,
                                DtoFactory factory,
                                BackgroundLoaderPresenter dependencyResolver,
                                PomEditorReconciler pomEditorReconciler,
                                WsAgentStateController agentStateController,
                                AppContext appContext) {
        this.eventBus = eventBus;

        this.notificationManager = notificationManager;
        this.dependencyResolver = dependencyResolver;
        this.pomEditorReconciler = pomEditorReconciler;
        this.appContext = appContext;

        handleOperations(factory, agentStateController);
    }

    private void handleOperations(final DtoFactory factory, final WsAgentStateController agentStateController) {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                agentStateController.getMessageBus().then(new Operation<MessageBus>() {
                    @Override
                    public void apply(MessageBus messageBus) throws OperationException {
                        try {
                            messageBus.subscribe(MavenAttributes.MAVEN_CHANEL_NAME, new MessageHandler() {
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

        int updatedNumber = 0;
        String deletedProject = "";

        for (String path : dto.getDeletedProjects()) {
            if (!updatedProjects.contains(path)) {
                updatedNumber++;
                deletedProject = path;
            }
        }

        if (updatedNumber > 1) {
            notificationManager.notify("Maven", updatedNumber + " modules were deleted", StatusNotification.Status.SUCCESS, EMERGE_MODE);
        } else if (updatedNumber == 1) {
            notificationManager.notify("Maven", "Module was deleted: " + deletedProject, StatusNotification.Status.SUCCESS, EMERGE_MODE);
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
