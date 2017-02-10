/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.ide.commit;

import com.google.common.base.Joiner;
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
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.commit.CommitView.ActionDelegate;
import org.eclipse.che.plugin.svn.ide.commit.diff.DiffViewerPresenter;
import org.eclipse.che.plugin.svn.ide.common.StatusColors;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsoleFactory;
import org.eclipse.che.plugin.svn.shared.CLIOutputParser;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;
import org.eclipse.che.plugin.svn.shared.StatusItem;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.resource.Path.valueOf;

/**
 * Presenter for the {@link CommitView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class CommitPresenter extends SubversionActionPresenter implements ActionDelegate {

    private final SubversionClientService                  service;
    private final CommitView                               view;
    private final SubversionCredentialsDialog              subversionCredentialsDialog;
    private final DiffViewerPresenter                      diffViewerPresenter;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;

    private enum Changes {
        ALL,
        SELECTION
    }

    private Map<Changes, List<StatusItem>> cache = new HashMap<>();

    @Inject
    public CommitPresenter(AppContext appContext,
                           CommitView view,
                           NotificationManager notificationManager,
                           SubversionOutputConsoleFactory consoleFactory,
                           SubversionExtensionLocalizationConstants constants,
                           SubversionClientService service,
                           SubversionCredentialsDialog subversionCredentialsDialog,
                           ProcessesPanelPresenter processesPanelPresenter,
                           DiffViewerPresenter diffViewerPresenter,
                           StatusColors statusColors) {
        super(appContext, consoleFactory, processesPanelPresenter, statusColors, constants, notificationManager, subversionCredentialsDialog);
        this.service = service;
        this.view = view;
        this.subversionCredentialsDialog = subversionCredentialsDialog;
        this.diffViewerPresenter = diffViewerPresenter;
        this.view.setDelegate(this);
        this.notificationManager = notificationManager;
        this.constants = constants;
    }

    public void show() {
        loadAllChanges();
    }

    private void loadAllChanges() {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        service.status(project.getLocation(), new Path[0], null, false, false, false, true, false, null)
               .then(new Operation<CLIOutputResponse>() {
                   @Override
                   public void apply(CLIOutputResponse response) throws OperationException {
                       List<StatusItem> statusItems = parseChangesList(response);
                       view.setChangesList(statusItems);
                       view.onShow();

                       cache.put(Changes.ALL, statusItems);
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError error) throws OperationException {
                       Log.error(CommitPresenter.class, error.getMessage());
                   }
               });
    }

    private List<StatusItem> parseChangesList(CLIOutputResponse response) {
        return CLIOutputParser.parseFilesStatus(response.getOutput());
    }

    private void loadSelectionChanges() {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));

        service.status(project.getLocation(), toRelative(project, resources), null, false, false, false, true, false, null)
               .then(new Operation<CLIOutputResponse>() {
                   @Override
                   public void apply(CLIOutputResponse response) throws OperationException {
                       List<StatusItem> statusItems = parseChangesList(response);
                       view.setChangesList(statusItems);

                       cache.put(Changes.SELECTION, statusItems);
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError error) throws OperationException {
                       Log.error(CommitPresenter.class, error.getMessage());
                   }
               });
    }

    /** {@inheritDoc} */
    @Override
    public void onCommitModeChanged() {
        if (view.isCommitAllSelected()) {
            view.setChangesList(cache.get(Changes.ALL));
        } else if (view.isCommitSelectionSelected()) {
            if (cache.containsKey(Changes.SELECTION)) {
                view.setChangesList(cache.get(Changes.SELECTION));
                return;
            }

            loadSelectionChanges();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        cache.clear();
        view.onClose();
    }

    /** {@inheritDoc} */
    @Override
    public void onCommitClicked() {
        final String message = view.getMessage();
        final boolean keepLocks = view.isKeepLocksStateSelected();

        if (view.isCommitSelectionSelected()) {
            commitSelection(message, keepLocks);
        } else if (view.isCommitAllSelected()) {
            commitAll(message, keepLocks);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showDiff(final String path) {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        performOperationWithCredentialsRequestIfNeeded(new RemoteSubversionOperation<CLIOutputResponse>() {
            @Override
            public Promise<CLIOutputResponse> perform(Credentials credentials) {
                return service.showDiff(project.getLocation(),
                                        new Path[]{valueOf(path)},
                                        "HEAD",
                                        credentials);
            }
        }, null).then(new Operation<CLIOutputResponse>() {
            @Override
            public void apply(CLIOutputResponse response) throws OperationException {
                String content = Joiner.on('\n').join(response.getOutput());
                diffViewerPresenter.showDiff(content);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
            }
        });
    }

    private void commitSelection(String message, boolean keepLocks) {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        final Resource[] resources = appContext.getResources();

        checkState(!Arrays.isNullOrEmpty(resources));

        doCommit(message, toRelative(project, resources), keepLocks);
    }

    private void commitAll(String message, boolean keepLocks) {
        doCommit(message, new Path[]{valueOf(".")}, keepLocks);
    }

    private void doCommit(String message, Path[] paths, boolean keepLocks) {
        final Project project = appContext.getRootProject();

        checkState(project != null);

        service.commit(project.getLocation(), paths, message, false, keepLocks).then(new Operation<CLIOutputWithRevisionResponse>() {
            @Override
            public void apply(CLIOutputWithRevisionResponse response) throws OperationException {
                printResponse(response.getCommand(), response.getOutput(), response.getErrOutput(), constants.commandCommit());
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                final StatusNotification notification = new StatusNotification(error.getMessage(), FAIL, FLOAT_MODE);
                notificationManager.notify(notification);
            }
        });

        view.onClose();
    }
}
