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
package org.eclipse.che.plugin.svn.ide.update;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Handler for the {@link import UpdateAction} action.
 */
@Singleton
public class UpdatePresenter extends SubversionActionPresenter {

    private final DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private final NotificationManager                      notificationManager;
    private final SubversionClientService                  service;
    private       EventBus                                 eventBus;
    private final SubversionExtensionLocalizationConstants constants;

    @Inject
    public UpdatePresenter(final AppContext appContext,
                           final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                           final EventBus eventBus,
                           final SubversionOutputConsoleFactory consoleFactory,
                           final SubversionClientService service,
                           final ConsolesPanelPresenter consolesPanelPresenter,
                           final SubversionExtensionLocalizationConstants constants,
                           final NotificationManager notificationManager,
                           final ProjectExplorerPresenter projectExplorerPart) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart);

        this.eventBus = eventBus;
        this.constants = constants;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.service = service;
    }

    public void showUpdate() {
        doUpdate("HEAD", "infinity", false, null);
    }

    protected void doUpdate(final String revision, final String depth, final boolean ignoreExternals,
                            final UpdateToRevisionView view) {
        final String projectPath = getCurrentProjectPath();
        if (projectPath == null) {
            return;
        }

        final StatusNotification notification = new StatusNotification(constants.updateToRevisionStarted(revision), PROGRESS, true);
        notificationManager.notify(notification);

        // TODO: Add UI widget for "Accept" part of update

        service.update(projectPath, getSelectedPaths(), revision, depth, ignoreExternals, "postpone",
                       new AsyncRequestCallback<CLIOutputWithRevisionResponse>(
                               dtoUnmarshallerFactory.newUnmarshaller(CLIOutputWithRevisionResponse.class)) {
                           @Override
                           protected void onSuccess(final CLIOutputWithRevisionResponse response) {
                               printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(),
                                             constants.commandUpdate());

                               notification.setTitle(constants.updateSuccessful(Long.toString(response.getRevision())));
                               notification.setStatus(SUCCESS);

                               if (view != null) {
                                   view.close();
                               }

                               eventBus.fireEvent(new SubversionProjectUpdatedEvent(response.getRevision()));
                           }

                           @Override
                           protected void onFailure(final Throwable exception) {
                               String errorMessage = exception.getMessage();

                               notification.setTitle(constants.updateFailed() + ": " + errorMessage);
                               notification.setStatus(FAIL);
                           }
                       });
    }

}
