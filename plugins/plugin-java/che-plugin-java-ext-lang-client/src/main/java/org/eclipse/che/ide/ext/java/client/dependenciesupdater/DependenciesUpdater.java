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
package org.eclipse.che.ide.ext.java.client.dependenciesupdater;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.event.DependencyUpdatedEvent;
import org.eclipse.che.ide.ext.java.client.project.node.jar.ExternalLibrariesNode;
import org.eclipse.che.ide.ext.java.shared.dto.ClassPathBuilderResult;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.rest.RequestCallback;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.ext.java.shared.dto.ClassPathBuilderResult.Status.SUCCESS;

/**
 * Updates dependencies for Maven project.
 *
 * @author Artem Zatsarynnyi
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class DependenciesUpdater {
    private final NotificationManager             notificationManager;
    private final AppContext                      appContext;
    private final DtoUnmarshallerFactory          dtoUnmarshallerFactory;
    private final JavaClasspathServiceClient      classpathServiceClient;
    private final JavaLocalizationConstant        locale;
    private final ProjectExplorerPresenter        projectExplorer;
    private final EventBus                        eventBus;
    private final Provider<LogsOutputHandler>     outputHandlerProvider;
    private final Map<String, StatusNotification> notifications;

    @Inject
    public DependenciesUpdater(JavaLocalizationConstant locale,
                               NotificationManager notificationManager,
                               AppContext appContext,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               JavaClasspathServiceClient classpathServiceClient,
                               EventBus eventBus,
                               ProjectExplorerPresenter projectExplorer,
                               Provider<LogsOutputHandler> outputHandlerProvider) {
        this.locale = locale;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.classpathServiceClient = classpathServiceClient;
        this.eventBus = eventBus;
        this.projectExplorer = projectExplorer;
        this.outputHandlerProvider = outputHandlerProvider;
        this.notifications = new HashMap<>();
    }

    public void updateDependencies(ProjectConfig config) {
        if (appContext.getCurrentProject() == null) {
            return;
        }

        final String path = config.getPath();

        final StatusNotification notification = new StatusNotification(locale.updatingDependencies(path), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        Unmarshallable<ClassPathBuilderResult> unmarshaller = dtoUnmarshallerFactory.newWSUnmarshaller(ClassPathBuilderResult.class);

        final String channel = "dependencyUpdate:output:" + appContext.getWorkspace().getId() + ':' + path;

        notifications.put(channel, notification);

        final LogsOutputHandler logsOutputHandler = outputHandlerProvider.get();

        classpathServiceClient.updateDependencies(path, new RequestCallback<ClassPathBuilderResult>(unmarshaller) {
            @Override
            protected void onSuccess(ClassPathBuilderResult result) {
                String updatedChannel = result.getChannel();

                StatusNotification notification = notifications.get(updatedChannel);

                if (SUCCESS.equals(result.getStatus())) {
                    onUpdated(updatedChannel, notification);
                } else {
                    updateFinishedWithError(locale.updateDependenciesFailed(), notification);
                }
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.warn(DependenciesUpdater.class, "Failed to launch update dependency process for " + path);
                updateFinishedWithError(exception.getMessage(), notification);
            }
        });

        String moduleName = path.substring(path.lastIndexOf('/'));

        logsOutputHandler.subscribeToOutput(channel, locale.dependenciesOutputTabTitle(moduleName));
    }

    private void onUpdated(String channel, StatusNotification notification) {
        notification.setContent(locale.dependenciesSuccessfullyUpdated());
        notification.setStatus(StatusNotification.Status.SUCCESS);
        projectExplorer.reloadChildrenByType(ExternalLibrariesNode.class);
        eventBus.fireEvent(new DependencyUpdatedEvent(channel));
    }

    private void updateFinishedWithError(String message, StatusNotification notification) {
        notification.setContent(message);
        notification.setStatus(StatusNotification.Status.FAIL);
    }
}
