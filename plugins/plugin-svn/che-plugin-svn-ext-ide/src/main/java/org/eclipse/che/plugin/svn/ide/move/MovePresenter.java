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
package org.eclipse.che.plugin.svn.ide.move;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.subversion.Credentials;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Presenter for the {@link MoveView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class MovePresenter extends SubversionActionPresenter implements MoveView.ActionDelegate {

    private final MoveView                                 view;
    private final SubversionExtensionLocalizationConstants locale;
    private final NotificationManager                      notificationManager;
    private final SubversionClientService                  service;

    private Project  project;
    private Resource source;

    @Inject
    public MovePresenter(AppContext appContext,
                         SubversionOutputConsoleFactory consoleFactory,
                         SubversionCredentialsDialog subversionCredentialsDialog,
                         ProcessesPanelPresenter processesPanelPresenter,
                         MoveView view,
                         NotificationManager notificationManager,
                         SubversionClientService service,
                         SubversionExtensionLocalizationConstants locale,
                         StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors, locale, notificationManager, subversionCredentialsDialog);
        this.notificationManager = notificationManager;
        this.service = service;

        this.view = view;
        this.locale = locale;
        this.view.setDelegate(this);
    }

    public void showMove() {

        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));
        checkState(resources.length == 1);

        source = resources[0];
        this.project = project;

        view.onShow(true);
        view.setProject(project);

    }

    /** {@inheritDoc} */
    @Override
    public void onMoveClicked() {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Path source = getSource();
        final String comment = view.isURLSelected() ? view.getComment() : null;

        final StatusNotification notification =
                new StatusNotification(locale.moveNotificationStarted(source.toString()), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        performOperationWithCredentialsRequestIfNeeded(new RemoteSubversionOperation<CLIOutputResponse>() {
            @Override
            public Promise<CLIOutputResponse> perform(Credentials credentials) {
                notification.setStatus(PROGRESS);
                notification.setTitle(locale.moveNotificationStarted(source.toString()));

                return service.move(project.getLocation(), source, getTarget(), comment, credentials);
            }
        }, notification).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse response) throws OperationException {
                notification.setTitle(locale.moveNotificationSuccessful());
                notification.setStatus(SUCCESS);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notification.setTitle(locale.moveNotificationFailed());
                notification.setStatus(FAIL);
            }
        });

        view.onClose();
    }

    private Path getSource() {
        if (view.isURLSelected()) {
            return Path.valueOf(view.getSourceUrl());
        } else {
            return toRelative(project, source);
        }
    }

    private Path getTarget() {
        if (view.isURLSelected()) {
            return Path.valueOf(view.getTargetUrl());
        } else {
            return toRelative(project, view.getDestinationNode());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.onClose();
    }

    /** {@inheritDoc} */
    @Override
    public void onUrlsChanged() {
        if (Strings.isNullOrEmpty(view.getSourceUrl())) {
            view.showErrorMarker(locale.moveSourceUrlEmpty());
            return;
        }

        if (Strings.isNullOrEmpty(view.getTargetUrl())) {
            view.showErrorMarker(locale.moveTargetUrlEmpty());
            return;
        }

        if (!getHostName(view.getSourceUrl()).equals(getHostName(view.getTargetUrl()))) {
            view.showErrorMarker(locale.moveSourceAndTargetNotEquals());
            return;
        }

        if (Strings.isNullOrEmpty(view.getComment())) {
            view.showErrorMarker(locale.moveCommentEmpty());
            return;
        }

        view.hideErrorMarker();
    }

    private static native String getHostName(String url) /*-{
        var parser = document.createElement('a')
        parser.href = url;
        return parser.hostname
    }-*/;
}
