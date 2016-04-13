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

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.action.UnlockAction;
import org.eclipse.che.plugin.svn.ide.common.PathTypeFilter;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.ide.common.threechoices.ChoiceDialog;
import org.eclipse.che.plugin.svn.ide.common.threechoices.ChoiceDialogFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Handler for the {@link org.eclipse.che.plugin.svn.ide.action.LockAction} and {@link UnlockAction} actions.
 */
public class LockUnlockPresenter extends SubversionActionPresenter {

    private final DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private final NotificationManager                      notificationManager;
    private final SubversionClientService                  service;
    private final SubversionExtensionLocalizationConstants constants;

    private final ChoiceDialogFactory choiceDialogFactory;
    private final DialogFactory dialogFactory;

    @Inject
    protected LockUnlockPresenter(final AppContext appContext,
                                  final DialogFactory dialogFactory,
                                  final ChoiceDialogFactory choiceDialogFactory,
                                  final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                  final NotificationManager notificationManager,
                                  final SubversionOutputConsoleFactory consoleFactory,
                                  final ConsolesPanelPresenter consolesPanelPresenter,
                                  final SubversionExtensionLocalizationConstants constants,
                                  final SubversionClientService service,
                                  final ProjectExplorerPresenter projectExplorerPart,
                                  final StatusColors statusColors) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart, statusColors);

        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.choiceDialogFactory = choiceDialogFactory;
        this.dialogFactory = dialogFactory;
    }

    public void showLockDialog() {
        showDialog(true);
    }

    public void showUnlockDialog() {
        showDialog(false);
    }

    private void showDialog(final boolean lock) {
        final String projectPath = getCurrentProjectPath();

        if (projectPath == null) {
            return;
        }

        final Collection<PathTypeFilter> filter = new ArrayList<>();
        filter.add(PathTypeFilter.FOLDER);
        filter.add(PathTypeFilter.PROJECT);
        final List<String> selectedFolders = getSelectedPaths(filter);
        if (!selectedFolders.isEmpty()) {
            this.dialogFactory.createMessageDialog(getLockDirectoryTitle(lock),
                                                   getLockDirectoryErrorMessage(lock), null).show();
            return;
        }

        final List<String> selectedPaths = getSelectedPaths(Collections.singletonList(PathTypeFilter.FILE));

        final String withoutForceLabel = getWithoutForceLabel(lock);
        final String withForceLabel = getWithForceLabel(lock);
        final String cancelLabel = getCancelLabel(lock);

        final ConfirmCallback withoutForceCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                doAction(lock, false, selectedPaths);
            }
        };
        final ConfirmCallback withForceCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                doAction(lock, true, selectedPaths);
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

    private String getLockDirectoryTitle(final boolean lock) {
        if (lock) {
            return constants.dialogTitleLockDirectory();
        } else {
            return constants.dialogTitleUnlockDirectory();
        }
    }

    private String getLockDirectoryErrorMessage(final boolean lock) {
        if (lock) {
            return constants.errorMessageLockDirectory();
        } else {
            return constants.errorMessageUnlockDirectory();
        }
    }

    private void doAction(final boolean lock, final boolean force, final List<String> paths) {
        if (lock) {
            doLockAction(force, paths);
        } else {
            doUnlockAction(force, paths);
        }
    }

    private void doLockAction(final boolean force, final List<String> paths) {
        final AsyncRequestCallback<CLIOutputResponse> callback = makeCallback(true);
        this.service.lock(getCurrentProjectPath(), paths, force, callback);
    }

    private void doUnlockAction(final boolean force, final List<String> paths) {
        final AsyncRequestCallback<CLIOutputResponse> callback = makeCallback(false);
        this.service.unlock(getCurrentProjectPath(), paths, force, callback);
    }

    private AsyncRequestCallback<CLIOutputResponse> makeCallback(final boolean lock) {
        final Unmarshallable<CLIOutputResponse> unmarshaller = this.dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class);
        return new AsyncRequestCallback<CLIOutputResponse>(unmarshaller) {
            @Override
            protected void onSuccess(final CLIOutputResponse result) {
                printResponse(result.getCommand(), result.getOutput(), result.getErrOutput(),
                              (lock ? constants.commandLock() : constants.commandUnlock()));
            }
            @Override
            protected void onFailure(final Throwable exception) {
                handleError(exception);
            }
        };
    }

    private void handleError(@NotNull final Throwable e) {
        String errorMessage;
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            errorMessage = e.getMessage();
        } else {
            errorMessage = constants.commitFailed();
        }
        this.notificationManager.notify(errorMessage, FAIL, true);
    }
}
