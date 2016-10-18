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
package org.eclipse.che.plugin.svn.ide.lockunlock;

import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.subversion.Credentials;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.action.UnlockAction;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.ide.common.threechoices.ChoiceDialog;
import org.eclipse.che.plugin.svn.ide.common.threechoices.ChoiceDialogFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Handler for the {@link org.eclipse.che.plugin.svn.ide.action.LockAction} and {@link UnlockAction} actions.
 */
public class LockUnlockPresenter extends SubversionActionPresenter {

    private final NotificationManager                      notificationManager;
    private final SubversionClientService                  service;
    private final SubversionExtensionLocalizationConstants constants;

    private final ChoiceDialogFactory choiceDialogFactory;

    @Inject
    protected LockUnlockPresenter(AppContext appContext,
                                  ChoiceDialogFactory choiceDialogFactory,
                                  NotificationManager notificationManager,
                                  SubversionOutputConsoleFactory consoleFactory,
                                  SubversionCredentialsDialog subversionCredentialsDialog,
                                  ProcessesPanelPresenter processesPanelPresenter,
                                  SubversionExtensionLocalizationConstants constants,
                                  SubversionClientService service,
                                  StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors, constants, notificationManager, subversionCredentialsDialog);

        this.service = service;
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.choiceDialogFactory = choiceDialogFactory;
    }

    public void showLockDialog() {
        showDialog(true);
    }

    public void showUnlockDialog() {
        showDialog(false);
    }

    private void showDialog(final boolean lock) {

        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));

        final String withoutForceLabel = getWithoutForceLabel(lock);
        final String withForceLabel = getWithForceLabel(lock);
        final String cancelLabel = getCancelLabel(lock);

        final ConfirmCallback withoutForceCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                doAction(lock, false, toRelative(project, resources));
            }
        };
        final ConfirmCallback withForceCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                doAction(lock, true, toRelative(project, resources));
            }
        };
        final ChoiceDialog dialog = this.choiceDialogFactory.createChoiceDialog(getTitle(lock), getContent(lock),
                                                                                withoutForceLabel, withForceLabel, cancelLabel,
                                                                                withoutForceCallback, withForceCallback,
                                                                                null);
        dialog.show();
    }

    String getTitle(final boolean lock) {
        if (lock) {
            return constants.lockDialogTitle();
        } else {
            return constants.unlockDialogTitle();
        }
    }

    private String getContent(final boolean lock) {
        if (lock) {
            return constants.lockDialogContent();
        } else {
            return constants.unlockDialogContent();
        }
    }

    private String getWithoutForceLabel(final boolean lock) {
        if (lock) {
            return constants.lockButtonWithoutForceLabel();
        } else {
            return constants.unlockButtonWithoutForceLabel();
        }
    }

    private String getWithForceLabel(final boolean lock) {
        if (lock) {
            return constants.lockButtonWithForceLabel();
        } else {
            return constants.unlockButtonWithForceLabel();
        }
    }

    private String getCancelLabel(final boolean lock) {
        return constants.buttonCancel();
    }

    private void doAction(final boolean lock, final boolean force, final Path[] paths) {
        if (lock) {
            doLockAction(force, paths);
        } else {
            doUnlockAction(force, paths);
        }
    }

    private void doLockAction(final boolean force, final Path[] paths) {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        performOperationWithCredentialsRequestIfNeeded(new RemoteSubversionOperation<CLIOutputResponse>() {
            @Override
            public Promise<CLIOutputResponse> perform(Credentials credentials) {
                return service.lock(project.getLocation(), paths, force, credentials);
            }
        }, null).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse response) throws OperationException {
                printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(), constants.commandLock());
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
            }
        });
    }

    private void doUnlockAction(final boolean force, final Path[] paths) {

        final Project project = appContext.getRootProject();

        checkState(project != null);

        performOperationWithCredentialsRequestIfNeeded(new RemoteSubversionOperation<CLIOutputResponse>() {
            @Override
            public Promise<CLIOutputResponse> perform(Credentials credentials) {
                return service.unlock(project.getLocation(), paths, force, credentials);
            }
        }, null).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse response) throws OperationException {
                printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(), constants.commandUnlock());
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
            }
        });
    }
}
