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
package org.eclipse.che.ide.factory.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.*;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.projectimport.wizard.ProjectImportOutputJsonRpcNotifier;
import org.eclipse.che.ide.projectimport.wizard.ProjectImporter;
import org.eclipse.che.ide.projectimport.wizard.ProjectResolver;
import org.eclipse.che.ide.ui.dialogs.askcredentials.AskCredentialsDialog;

import java.util.*;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;

/**
 * Imports projects on file system.
 */
@Singleton
public class InitialProjectImporter extends ProjectImporter {

    private final ProjectImportOutputJsonRpcNotifier subscriber;
    private final NotificationManager notificationManager;
    private final CoreLocalizationConstant locale;

    @Inject
    public InitialProjectImporter(CoreLocalizationConstant localizationConstant,
                                  ImportProjectNotificationSubscriberFactory subscriberFactory,
                                  AppContext appContext,
                                  ProjectResolver projectResolver,
                                  AskCredentialsDialog credentialsDialog,
                                  OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry,
                                  ProjectImportOutputJsonRpcNotifier subscriber,
                                  NotificationManager notificationManager,
                                  CoreLocalizationConstant locale,
                                  ProjectExplorerPresenter projectExplorerPresenter) {

        super(localizationConstant, subscriberFactory, appContext, projectResolver, credentialsDialog, oAuth2AuthenticatorRegistry);

        this.subscriber = subscriber;
        this.notificationManager = notificationManager;
        this.locale = locale;
    }

    /**
     * Import source projects and if it's already exist in workspace
     * then show warning notification
     *
     * @param projects
     *         list of projects that already exist in workspace and will be imported on file system
     */
    public void importProjects(final List<Project> projects) {
        if (projects.isEmpty()) {
            return;
        }

        final Project importProject = projects.remove(0);
        final StatusNotification notification = notificationManager.notify(locale.cloningSource(importProject.getName()), null, PROGRESS, FLOAT_MODE);
        subscriber.subscribe(importProject.getName(), notification);

        appContext.getWorkspaceRoot()
                  .importProject()
                  .withBody(importProject)
                  .send()
                  .then(new Operation<Project>() {
                      @Override
                      public void apply(Project project) throws OperationException {
                          subscriber.onSuccess();

                          appContext.getWorkspaceRoot().synchronize();

                          importProjects(projects);
                      }
                  }).catchErrorPromise(
                  new Function<PromiseError, Promise<Project>>() {
                      @Override
                      public Promise<Project> apply(PromiseError err) throws FunctionException {
                          subscriber.onFailure(err.getMessage());
                          notification.setTitle(locale.cloningSourceFailedTitle(importProject.getName()));
                          notification.setStatus(FAIL);

                          return Promises.resolve(null);
                      }
                  }
        );
    }

}
