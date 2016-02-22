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
package org.eclipse.che.ide.extension.maven.client.comunnication;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
import org.eclipse.che.ide.extension.maven.shared.MessageType;
import org.eclipse.che.ide.extension.maven.shared.dto.NotificationMessage;
import org.eclipse.che.ide.extension.maven.shared.dto.ProjectsUpdateMessage;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;

import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenMessagesHandler {

    private final EventBus                 eventBus;
    private final NotificationManager      notificationManager;
    private final ProjectExplorerPresenter projectExplorer;
    private final ProjectServiceClient     projectServiceClient;
    private final AppContext               context;
    private final PomEditorReconciler      pomEditorReconciler;
    private       Notification             notification;

    @Inject
    public MavenMessagesHandler(EventBus eventBus,
                                final MessageBusProvider messageBus,
                                NotificationManager notificationManager,
                                final DtoFactory factory,
                                final ProjectExplorerPresenter projectExplorer,
                                ProjectServiceClient projectServiceClient,
                                AppContext context,
                                PomEditorReconciler pomEditorReconciler) {
        this.eventBus = eventBus;

        this.notificationManager = notificationManager;
        this.projectExplorer = projectExplorer;
        this.projectServiceClient = projectServiceClient;
        this.context = context;
        this.pomEditorReconciler = pomEditorReconciler;
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                try {
                    messageBus.getMachineMessageBus().subscribe(MavenAttributes.MAVEN_CHANEL_NAME, new MessageHandler() {
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
                                default:
                                    Log.error(getClass(), "Unknown message type:" + messageType);
                            }
                        }
                    });
                } catch (WebSocketException e) {
                    Log.error(getClass(), e);
                }
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {

            }
        });

    }

    private void handleUpdate(ProjectsUpdateMessage dto) {
        List<String> updatedProjects = dto.getUpdatedProjects();
        for (String path : updatedProjects) {
            final String projectPath = path;
            Promise<ProjectConfigDto> promise = projectServiceClient.getProject(context.getWorkspaceId(), path);
            promise.then(new Operation<ProjectConfigDto>() {
                @Override
                public void apply(ProjectConfigDto arg) throws OperationException {
                    eventBus.fireEvent(new ProjectUpdatedEvent(projectPath, arg));
                }
            });
        }
        for (String path : dto.getDeletedProjects()) {
            if (!updatedProjects.contains(path)) {
                notificationManager.notify("Maven", "Module was deleted: " + path, StatusNotification.Status.SUCCESS, true);
            }
        }

       pomEditorReconciler.updateProblems(dto.getProblems());
    }

    private void handleNotification(NotificationMessage message) {
        if(notification == null){
            notification = notificationManager.notify("Maven", "", StatusNotification.Status.PROGRESS, true);
        }
        notification.setContent(message.getText());
        notification.notifyObservers();

    }
}
