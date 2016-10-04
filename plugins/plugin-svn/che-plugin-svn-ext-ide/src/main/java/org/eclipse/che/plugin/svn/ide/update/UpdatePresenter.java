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

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.subversion.Credentials;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_SVN_OPERATION;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Handler for the {@link import UpdateAction} action.
 */
@Singleton
public class UpdatePresenter extends SubversionActionPresenter {

    private final NotificationManager                      notificationManager;
    private final SubversionClientService                  service;
    private final SubversionCredentialsDialog              subversionCredentialsDialog;
    private final SubversionExtensionLocalizationConstants constants;

    @Inject
    public UpdatePresenter(AppContext appContext,
                           SubversionOutputConsoleFactory consoleFactory,
                           SubversionClientService service,
                           SubversionCredentialsDialog subversionCredentialsDialog,
                           ProcessesPanelPresenter processesPanelPresenter,
                           SubversionExtensionLocalizationConstants constants,
                           NotificationManager notificationManager,
                           StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors);
        this.subversionCredentialsDialog = subversionCredentialsDialog;

        this.constants = constants;
        this.notificationManager = notificationManager;
        this.service = service;
    }

    public void showUpdate() {
        doUpdate("HEAD", "infinity", false, null);
    }

    void doUpdate(final String revision, final String depth, final boolean ignoreExternals, final UpdateToRevisionView view) {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));

        final StatusNotification notification = new StatusNotification(constants.updateToRevisionStarted(revision), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        doUpdate(revision, depth, ignoreExternals, view, project, resources, notification, null);
    }

    private void doUpdate(final String revision,
                          final String depth,
                          final boolean ignoreExternals,
                          final UpdateToRevisionView view,
                          final Project project,
                          final Resource[] resources,
                          final StatusNotification notification,
                          final Credentials credentials) {
        service.update(project.getLocation(),
                       toRelative(project, resources),
                       revision,
                       depth,
                       ignoreExternals,
                       "postpone",
                       credentials)
               .then(new Operation<CLIOutputWithRevisionResponse>() {
                   @Override
                   public void apply(CLIOutputWithRevisionResponse response) throws OperationException {
                       printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(),
                                     constants.commandUpdate());

                       notification.setTitle(constants.updateSuccessful(Long.toString(response.getRevision())));
                       notification.setStatus(SUCCESS);

                       if (view != null) {
                           view.close();
                       }
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(final PromiseError error) throws OperationException {
                       if (getErrorCode(error.getCause()) == UNAUTHORIZED_SVN_OPERATION) {
                           tryWithCredentials(notificationManager,
                                              subversionCredentialsDialog,
                                              constants.authenticationFailed(),
                                              new SVNOperation() {
                                                  @Override
                                                  public void perform(Credentials credentials) {
                                                      doUpdate(revision,
                                                               depth,
                                                               ignoreExternals,
                                                               view,
                                                               project,
                                                               resources,
                                                               notification,
                                                               credentials);
                                                  }
                                              });
                       } else {
                           notification.setTitle(constants.updateFailed());
                           notification.setStatus(FAIL);
                       }
                   }
               });
    }

}
