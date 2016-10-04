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

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;

/**
 * Handler for the {@link org.eclipse.che.plugin.svn.ide.action.CheckoutAction} action.
 */
@Singleton
public class SwitchPresenter implements SwitchView.ActionDelegate {

    private final AppContext                               appContext;
    private final NotificationManager                      notificationManager;
    private final SubversionOutputConsoleFactory           consoleFactory;
    private final SubversionClientService                  service;
    private final SubversionExtensionLocalizationConstants constants;
    private final ProcessesPanelPresenter                  processesPanelPresenter;
    private final SwitchView                               view;
    private final StatusColors                             statusColors;

    @Inject
    public SwitchPresenter(AppContext appContext,
                           NotificationManager notificationManager,
                           SubversionOutputConsoleFactory consoleFactory,
                           SubversionClientService service,
                           SubversionExtensionLocalizationConstants constants,
                           ProcessesPanelPresenter processesPanelPresenter,
                           SwitchView view,
                           StatusColors statusColors) {
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.consoleFactory = consoleFactory;
        this.service = service;
        this.constants = constants;
        this.processesPanelPresenter = processesPanelPresenter;


        this.view = view;
        this.statusColors = statusColors;

        this.view.setDelegate(this);
    }

    /**
     * Displays the dialog and resets its state.
     */
    public void showWindow() {
        view.setDepth("infinity");
        view.setRevision("");
        view.setIgnoreExternals(false);
        view.setIsCustomRevision(false);
        view.setIsHeadRevision(true);

        view.setEnableUpdateButton(true);
        view.setEnableCustomRevision(false);

        view.showWindow();
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onUpdateClicked() {
        doCheckout(view.getRevision(), view.getDepth(), view.ignoreExternals(), view);
    }

    @Override
    public void onRevisionTypeChanged() {
        handleFormChange();
    }

    @Override
    public void onRevisionChanged() {
        handleFormChange();
    }

    /**
     * Helper method to enable/disable form fields based on form state changes.
     */
    private void handleFormChange() {
        view.setEnableCustomRevision(view.isCustomRevision());

        if (view.isCustomRevision() && view.getRevision().isEmpty()) {
            view.setEnableUpdateButton(false);
        } else {
            view.setEnableUpdateButton(true);
        }
    }

    public void showCheckout() {
        doCheckout("HEAD", "infinity", false, null);
    }

    protected void doCheckout(final String revision,
                              final String depth,
                              final boolean ignoreExternals,
                              final SwitchView view) {

        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));

        final StatusNotification notification = new StatusNotification(constants.updateToRevisionStarted(revision), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

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
