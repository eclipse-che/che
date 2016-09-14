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

import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponseList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

public class ResolvePresenter extends SubversionActionPresenter implements ResolveView.ActionDelegate {

    private final NotificationManager                      notificationManager;
    private final DialogFactory                            dialogFactory;
    private final SubversionExtensionLocalizationConstants constants;
    private final SubversionClientService                  service;
    private final ResolveView                              view;

    private List<String> conflictsList;

    @Inject
    protected ResolvePresenter(ProcessesPanelPresenter processesPanelPresenter,
                               SubversionOutputConsoleFactory consoleFactory,
                               AppContext appContext,
                               SubversionExtensionLocalizationConstants constants,
                               NotificationManager notificationManager,
                               DialogFactory dialogFactory,
                               SubversionClientService service,
                               ResolveView view,
                               StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors);

        this.service = service;
        this.notificationManager = notificationManager;
        this.dialogFactory = dialogFactory;
        this.constants = constants;
        this.view = view;

        this.view.setDelegate(this);
    }

    public void showConflictsDialog() {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));

        service.showConflicts(project.getLocation(), toRelative(project, resources)).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse response) throws OperationException {
                conflictsList = parseConflictsList(response.getOutput());

                if (conflictsList.isEmpty()) {
                    dialogFactory.createMessageDialog(constants.resolveNoConflictTitle(), constants.resolveNoConflictContent(), null)
                                 .show();

                    return;
                }

                for (String file : conflictsList) {
                    view.addConflictingFile(file);
                }
                view.showDialog();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {

            }
        });
    }

    protected List<String> parseConflictsList(List<String> output) {
        List<String> conflictsList = new ArrayList<>();
        for (String line : output) {
            if (line.startsWith("C ")) {
                int lastSpaceIndex = line.lastIndexOf(' ');
                String filePathMatched = line.substring(lastSpaceIndex + 1);
                conflictsList.add(filePathMatched);
            }
        }
        return conflictsList;
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onResolveClicked() {

        final Project project = appContext.getRootProject();

        checkState(project != null);

        HashMap<String, String> filesConflictResolutionActions = new HashMap<String, String>();
        Iterator<String> iterConflicts = conflictsList.iterator();

        while (iterConflicts.hasNext()) {
            String path = iterConflicts.next();
            String resolutionActionText = view.getConflictResolutionAction(path);
            if (!resolutionActionText.equals(ConflictResolutionAction.POSTPONE.getText())) {
                filesConflictResolutionActions.put(path, resolutionActionText);
                iterConflicts.remove();
            }
        }

        if (filesConflictResolutionActions.size() > 0) {
            service.resolve(project.getLocation(), filesConflictResolutionActions, "infinity").then(new Operation<CLIOutputResponseList>() {
                @Override
                public void apply(CLIOutputResponseList response) throws OperationException {
                    for (CLIOutputResponse outputResponse : response.getCLIOutputResponses()) {
                        printResponse(outputResponse.getCommand(), outputResponse.getOutput(), null, constants.commandResolve());
                    }
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError error) throws OperationException {
                    notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
                }
            });
        }
        view.close();
    }

}
