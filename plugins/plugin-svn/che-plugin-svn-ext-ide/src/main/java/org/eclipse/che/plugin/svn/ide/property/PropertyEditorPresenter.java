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
package org.eclipse.che.plugin.svn.ide.property;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.Depth;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Presenter for the {@link PropertyEditorView}.
 *
 * @author Vladyslav Zhukovskyi
 * @author Stephane Tournie
 */
@Singleton
public class PropertyEditorPresenter extends SubversionActionPresenter implements PropertyEditorView.ActionDelegate {

    private PropertyEditorView                       view;
    private SubversionClientService                  service;
    private NotificationManager                      notificationManager;
    private SubversionExtensionLocalizationConstants constants;

    @Inject
    protected PropertyEditorPresenter(AppContext appContext,
                                      SubversionOutputConsoleFactory consoleFactory,
                                      ProcessesPanelPresenter processesPanelPresenter,
                                      PropertyEditorView view,
                                      SubversionClientService service,
                                      SubversionCredentialsDialog credentialsDialog,
                                      NotificationManager notificationManager,
                                      SubversionExtensionLocalizationConstants constants,
                                      StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors, constants, notificationManager, credentialsDialog);
        this.view = view;
        this.service = service;
        this.notificationManager = notificationManager;
        this.constants = constants;
        view.setDelegate(this);
    }

    public void showEditor() {
        view.onShow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelClicked() {
        view.onClose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOkClicked() {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        view.onClose();

        if (view.isEditPropertySelected()) {
            editProperty(project);
        } else if (view.isDeletePropertySelected()) {
            deleteProperty(project);
        }
    }

    @Override
    public void onPropertyNameChanged(String propertyName) {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));
        checkState(resources.length == 1);

        service.propertyGet(project.getLocation(), propertyName, toRelative(project, resources[0]))
               .then(new Operation<CLIOutputResponse>() {
                    @Override
                    public void apply(CLIOutputResponse response) throws OperationException {
                        view.setPropertyCurrentValue(response.getOutput());
                    }
                })
                .catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError error) throws OperationException {
                        notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
                    }
                });
    }

    private void editProperty(Project project) {
        final String propertyName = view.getSelectedProperty();
        final Depth depth = view.getDepth();
        final String propertyValue = view.getPropertyValue();
        final boolean force = view.isForceSelected();

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));
        checkState(resources.length == 1);

        final StatusNotification notification = new StatusNotification(constants.propertyModifyStart(), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        service.propertySet(project.getLocation(), propertyName, propertyValue, depth, force, toRelative(project, resources[0]))
                .then(new Operation<CLIOutputResponse>() {
                    @Override
                    public void apply(CLIOutputResponse response) throws OperationException {
                        printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(), constants.commandProperty());

                        notification.setTitle(constants.propertyModifyFinished());
                        notification.setStatus(SUCCESS);
                    }
                })
                .catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError error) throws OperationException {
                        notification.setTitle(constants.propertyModifyFailed());
                        notification.setStatus(FAIL);
                    }
                });
    }

    private void deleteProperty(Project project) {
        final String propertyName = view.getSelectedProperty();
        final Depth depth = view.getDepth();
        final boolean force = view.isForceSelected();

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));
        checkState(resources.length == 1);

        final StatusNotification notification = new StatusNotification(constants.propertyRemoveStart(), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);

        service.propertyDelete(project.getLocation(), propertyName, depth, force, toRelative(project, resources[0]))
                .then(new Operation<CLIOutputResponse>() {
                    @Override
                    public void apply(CLIOutputResponse response) throws OperationException {
                        printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(), constants.commandProperty());

                        notification.setTitle(constants.propertyRemoveFinished());
                        notification.setStatus(SUCCESS);
                    }
                })
                .catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError error) throws OperationException {
                        notification.setTitle(constants.propertyRemoveFailed());
                        notification.setStatus(FAIL);
                    }
                });
    }

    @Override
    public void obtainExistingPropertiesForPath() {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));
        checkState(resources.length == 1);

        service.propertyList(project.getLocation(), toRelative(project, resources[0])).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse response) throws OperationException {
                List<String> properties = new ArrayList<String>();
                for (String property : response.getOutput()) {
                    properties.add(property.trim());
                }
                view.setExistingPropertiesForPath(properties);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE));
            }
        });
    }
}
