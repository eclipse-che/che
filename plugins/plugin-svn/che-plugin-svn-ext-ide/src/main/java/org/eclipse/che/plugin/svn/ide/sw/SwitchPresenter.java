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
package org.eclipse.che.plugin.svn.ide.sw;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;

import static com.google.common.base.Preconditions.checkState;

/**
 * Handler for the {@link org.eclipse.che.plugin.svn.ide.action.SwitchAction} action.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class SwitchPresenter implements SwitchView.ActionDelegate {

    private final AppContext                               appContext;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;
    private final SwitchView                               view;
    private final SubversionClientService                  service;

    private String location;

    @Inject
    public SwitchPresenter(AppContext appContext,
                           NotificationManager notificationManager,
                           SubversionExtensionLocalizationConstants constants,
                           SwitchView view,
                           SubversionClientService service) {
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.constants = constants;

        this.view = view;
        this.service = service;
        this.view.setDelegate(this);
    }

    public void showWindow() {
        final Project project = appContext.getRootProject();
        checkState(project != null);

        view.showWindow();
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onSwitchClicked() {
        doSwitch();
    }

    @Override
    public void onSwitchToTrunkChanged() {
        location = "^/trunk";
    }

    @Override
    public void onSwitchToBranchChanged() {
        service.list(appContext.getRootProject().getLocation(), Path.valueOf("^/branches")).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse arg) throws OperationException {
                arg.getOutput();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                arg.getMessage();
            }
        });
    }

    @Override
    public void onSwitchToTagChanged() {
        service.list(appContext.getRootProject().getLocation(), Path.valueOf("^/tags")).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse arg) throws OperationException {
                arg.getOutput();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                arg.getMessage();
            }
        });
    }

    @Override
    public void onSwitchToLocationChanged() {
    }

    protected void doSwitch() {
//        final StatusNotification notification = new StatusNotification(constants.updateToRevisionStarted(revision), PROGRESS, FLOAT_MODE);
//        notificationManager.notify(notification);

//        service.checkout(project.getLocation(), toRelative(project, resources), revision, depth, ignoreExternals, "postpone")
//               .then(new Operation<CLIOutputWithRevisionResponse>() {
//                   @Override
//                   public void apply(CLIOutputWithRevisionResponse response) throws OperationException {
//                       printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(),
//                                     constants.commandUpdate());
//
//                       notification.setTitle(constants.updateSuccessful(Long.toString(response.getRevision())));
//                       notification.setStatus(SUCCESS);
//
//                       if (view != null) {
//                           view.close();
//                       }
//                   }
//               })
//               .catchError(new Operation<PromiseError>() {
//                   @Override
//                   public void apply(PromiseError error) throws OperationException {
//                       notification.setTitle(constants.updateFailed());
//                       notification.setStatus(FAIL);
//                   }
//               });
    }
}
