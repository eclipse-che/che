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
package org.eclipse.che.plugin.svn.ide.resolve;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponseList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

public class ResolvePresenter extends SubversionActionPresenter implements ResolveView.ActionDelegate {

    private final NotificationManager                      notificationManager;
    private final DialogFactory                            dialogFactory;
    private final SubversionExtensionLocalizationConstants constants;
    private final SubversionClientService                  subversionClientService;
    private final ResolveView                              view;

    private List<String> conflictsPaths;

    @Inject
    protected ResolvePresenter(final ConsolesPanelPresenter consolesPanelPresenter,
                               final SubversionOutputConsoleFactory consoleFactory,
                               final AppContext appContext,
                               final SubversionExtensionLocalizationConstants constants,
                               final NotificationManager notificationManager,
                               final DialogFactory dialogFactory,
                               final SubversionClientService subversionClientService,
                               final ResolveView view,
                               final ProjectExplorerPresenter projectExplorerPart,
                               final StatusColors statusColors) {
        super(appContext, consoleFactory, consolesPanelPresenter, projectExplorerPart, statusColors);

        this.subversionClientService = subversionClientService;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
        this.constants = constants;
        this.view = view;

        this.view.setDelegate(this);
    }

    public boolean containsConflicts() {
        return conflictsPaths != null && !conflictsPaths.isEmpty();
    }

    public void fetchConflictsList(boolean forCurrentSelection) {
        CurrentProject currentProject = getActiveProject();
        if (currentProject == null) {
            return;
        }

        ProjectConfigDto project = currentProject.getRootProject();
        if (project == null) {
            return;
        }

        subversionClientService.showConflicts(project.getPath(),
                                              forCurrentSelection ? getSelectedPaths() : null,
                                              new AsyncCallback<List<String>>() {
                                                  @Override
                                                  public void onSuccess(List<String> conflictsList) {
                                                      conflictsPaths = conflictsList;
                                                  }

                                                  @Override
                                                  public void onFailure(Throwable exception) {
                                                      notificationManager.notify(exception.getMessage(), FAIL, FLOAT_MODE);
                                                  }
                                              });
    }

    public void showConflictsDialog() {
        if (conflictsPaths != null && !conflictsPaths.isEmpty()) {
            for (String file : conflictsPaths) {
                view.addConflictingFile(file);
            }
            view.showDialog();
        } else {
            dialogFactory.createMessageDialog(constants.resolveNoConflictTitle(), constants.resolveNoConflictContent(),
                                              new ConfirmCallback() {
                                                  @Override
                                                  public void accepted() {}
                                              }).show();
        }
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onResolveClicked() {
        CurrentProject currentProject = getActiveProject();
        if (currentProject == null) {
            return;
        }

        ProjectConfigDto project = currentProject.getRootProject();
        if (project == null) {
            return;
        }

        HashMap<String, String> filesConflictResolutionActions = new HashMap<String, String>();
        Iterator<String> iterConflicts = conflictsPaths.iterator();

        while (iterConflicts.hasNext()) {
            String path = iterConflicts.next();
            String resolutionActionText = view.getConflictResolutionAction(path);
            if (!resolutionActionText.equals(ConflictResolutionAction.POSTPONE.getText())) {
                filesConflictResolutionActions.put(path, resolutionActionText);
                iterConflicts.remove();
            }
        }

        if (filesConflictResolutionActions.size() > 0) {
            subversionClientService.resolve(project.getPath(), filesConflictResolutionActions, "infinity",
                                            new AsyncCallback<CLIOutputResponseList>() {
                                                @Override
                                                public void onSuccess(CLIOutputResponseList result) {
                                                    for (CLIOutputResponse outputResponse : result.getCLIOutputResponses()) {
                                                        printResponse(outputResponse.getCommand(), outputResponse.getOutput(), null,
                                                                      constants.commandResolve());
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Throwable exception) {
                                                    notificationManager.notify(exception.getMessage(), FAIL, FLOAT_MODE);
                                                }
                                            });
        }
        view.close();
    }

}
