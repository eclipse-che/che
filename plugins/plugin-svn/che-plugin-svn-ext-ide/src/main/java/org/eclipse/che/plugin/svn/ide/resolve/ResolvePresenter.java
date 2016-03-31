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
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsolePresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponseList;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import java.util.HashMap;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

public class ResolvePresenter extends SubversionActionPresenter implements ResolveView.ActionDelegate {

    private final NotificationManager                      notificationManager;
    private final DialogFactory                            dialogFactory;
    private final SubversionExtensionLocalizationConstants constants;
    private final SubversionClientService                  subversionClientService;
    private final ResolveView                              view;

    private List<String> conflictsPaths;

    @Inject
    protected ResolvePresenter(final EventBus eventBus,
                               final WorkspaceAgent workspaceAgent,
                               final SubversionOutputConsolePresenter console,
                               final AppContext appContext,
                               final SubversionExtensionLocalizationConstants constants,
                               final NotificationManager notificationManager,
                               final DialogFactory dialogFactory,
                               final SubversionClientService subversionClientService,
                               final ResolveView view,
                               final ProjectExplorerPresenter projectExplorerPart) {
        super(appContext, eventBus, console, workspaceAgent, projectExplorerPart);

        this.subversionClientService = subversionClientService;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
        this.constants = constants;
        this.view = view;

        this.view.setDelegate(this);
    }

    public void fetchConflictsList(boolean forCurrentSelection, final AsyncCallback<List<String>> callback) {
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
                                                      callback.onSuccess(conflictsList);
                                                  }

                                                  @Override
                                                  public void onFailure(Throwable exception) {
                                                      notificationManager.notify(exception.getMessage(), FAIL, true);
                                                  }
                                              });
    }

    public void showConflictsDialog(List<String> conflictsList) {
        if (conflictsList.size() > 0) {
            for (String file : conflictsList) {
                view.addConflictingFile(file);
            }
            conflictsPaths = conflictsList;
            view.showDialog();
        } else {
            dialogFactory.createMessageDialog(constants.resolveNoConflictTitle(), constants.resolveNoConflictContent(),
                                              new ConfirmCallback() {
                                                  @Override
                                                  public void accepted() {
                                                  }
                                              }).show();
        }
    }

    @Override
    public void onCancelClicked() {
        view.close();
        conflictsPaths.clear();
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
        for (String path : conflictsPaths) {
            String resolutionActionText = view.getConflictResolutionAction(path);
            filesConflictResolutionActions.put(path, resolutionActionText);
        }

        subversionClientService.resolve(project.getPath(), filesConflictResolutionActions, "infinity",
                                        new AsyncCallback<CLIOutputResponseList>() {
                                            @Override
                                            public void onSuccess(CLIOutputResponseList result) {
                                                for (CLIOutputResponse outputResponse : result.getCLIOutputResponses()) {
                                                    printCommand(outputResponse.getCommand());
                                                    printAndSpace(outputResponse.getOutput());
                                                }
                                            }

                                            @Override
                                            public void onFailure(Throwable exception) {
                                                notificationManager.notify(exception.getMessage(), FAIL, true);
                                            }
                                        });
        view.close();
        conflictsPaths.clear();
    }

}
