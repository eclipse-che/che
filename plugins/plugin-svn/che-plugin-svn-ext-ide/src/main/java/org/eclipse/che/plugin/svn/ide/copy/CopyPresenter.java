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
package org.eclipse.che.plugin.svn.ide.copy;

import com.google.common.base.Preconditions;
import com.google.gwt.regexp.shared.RegExp;
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
import org.eclipse.che.ide.util.RegExpUtils;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SvnUtil;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Presenter for the {@link CopyView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class CopyPresenter extends SubversionActionPresenter implements CopyView.ActionDelegate {

    private final CopyView                                 view;
    private final NotificationManager                      notificationManager;
    private final SubversionClientService                  service;
    private final SubversionExtensionLocalizationConstants constants;

    private RegExp urlRegExp = RegExp.compile("^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private Resource sourceNode;
    private Resource target;

    @Inject
    protected CopyPresenter(AppContext appContext,
                            SubversionOutputConsoleFactory consoleFactory,
                            SubversionCredentialsDialog subversionCredentialsDialog,
                            ProcessesPanelPresenter processesPanelPresenter,
                            CopyView view,
                            NotificationManager notificationManager,
                            SubversionClientService service,
                            SubversionExtensionLocalizationConstants constants,
                            StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors, constants, notificationManager, subversionCredentialsDialog);
        this.view = view;
        this.notificationManager = notificationManager;
        this.service = service;
        this.constants = constants;
        this.view.setDelegate(this);
    }

    /** Show copy dialog. */
    public void showCopy() {
        final Project project = appContext.getRootProject();

        Preconditions.checkState(project != null && SvnUtil.isUnderSvn(project));

        final Resource[] resources = appContext.getResources();

        Preconditions.checkState(resources != null && resources.length == 1);

        sourceNode = resources[0];

        if (sourceNode.getResourceType() == Resource.FILE) {
            view.setDialogTitle(constants.copyViewTitleFile());
        } else {
            view.setDialogTitle(constants.copyViewTitleDirectory());
        }

        view.setNewName(sourceNode.getName());
        view.setComment(sourceNode.getName());
        view.setSourcePath(sourceNode.getLocation().toString(), false);

        validate();

        view.show();
        view.setProjectNode(project);
    }

    /** {@inheritDoc} */
    @Override
    public void onCopyClicked() {
        final Project project = appContext.getRootProject();

        Preconditions.checkState(project != null);

        final Path src = view.isSourceCheckBoxSelected() ? Path.valueOf(view.getSourcePath()) : toRelative(project, sourceNode);
        final Path target = view.isTargetCheckBoxSelected() ? Path.valueOf(view.getTargetUrl()) : toRelative(project, this.target);
        final String comment = view.isTargetCheckBoxSelected() ? view.getComment() : null;

        final StatusNotification notification = new StatusNotification(constants.copyNotificationStarted(src.toString()),
                                                                       PROGRESS,
                                                                       FLOAT_MODE);
        notificationManager.notify(notification);

        view.hide();

        performOperationWithCredentialsRequestIfNeeded(new RemoteSubversionOperation<CLIOutputResponse>() {
            @Override
            public Promise<CLIOutputResponse> perform(Credentials credentials) {
                notification.setStatus(PROGRESS);
                notification.setTitle(constants.copyNotificationStarted(src.toString()));

                return service.copy(project.getLocation(), src, target, comment, credentials);
            }
        }, notification).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse response) throws OperationException {
                printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(), constants.commandCopy());

                notification.setTitle(constants.copyNotificationSuccessful());
                notification.setStatus(SUCCESS);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                notification.setTitle(constants.copyNotificationFailed());
                notification.setStatus(FAIL);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onNewNameChanged(String newName) {
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeSelected(Resource target) {
        this.target = target;
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onSourcePathChanged() {
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onTargetUrlChanged() {
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onSourceCheckBoxChanged() {
        // url path chosen
        if (view.isSourceCheckBoxSelected()) {
            view.setSourcePath("", true);
            view.setNewName("name");
        } else {
            view.setSourcePath(sourceNode.getLocation().toString(), false);
            view.setNewName(sourceNode.getName());
        }

        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onTargetCheckBoxChanged() {
        validate();
    }

    private void validate() {
        ValidationStrategy strategy;

        if (view.isSourceCheckBoxSelected()) {
            if (view.isTargetCheckBoxSelected()) {
                strategy = new UrlUrlValidation();
            } else {
                strategy = new UrlFileValidation();
            }
        } else {
            if (view.isTargetCheckBoxSelected()) {
                strategy = new FileUrlValidation();
            } else {
                strategy = new FileFileValidation();
            }
        }

        if (strategy.isValid()) {
            view.hideErrorMarker();
        }
    }

    private interface ValidationStrategy {
        boolean isValid();
    }

    private class FileFileValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (target == null) {
                view.showErrorMarker(constants.copyEmptyTarget());
                return false;
            }

            return true;
        }
    }

    private class UrlFileValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (target == null) {
                view.showErrorMarker(constants.copyEmptyTarget());
                return false;
            }

            if (!RegExpUtils.resetAndTest(urlRegExp, view.getSourcePath())) {
                view.showErrorMarker(constants.copySourceWrongURL());
                return false;
            }

            return true;
        }
    }

    private class FileUrlValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (!RegExpUtils.resetAndTest(urlRegExp, view.getTargetUrl())) {
                view.showErrorMarker(constants.copyTargetWrongURL());
                return false;
            }

            return true;
        }
    }

    private class UrlUrlValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (!RegExpUtils.resetAndTest(urlRegExp, view.getSourcePath())) {
                view.showErrorMarker(constants.copySourceWrongURL());
                return false;
            }

            if (!RegExpUtils.resetAndTest(urlRegExp, view.getTargetUrl())) {
                view.showErrorMarker(constants.copyTargetWrongURL());
                return false;
            }

            return true;
        }
    }
}
