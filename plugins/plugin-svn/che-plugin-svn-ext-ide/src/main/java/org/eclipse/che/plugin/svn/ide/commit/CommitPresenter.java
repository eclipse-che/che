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
package org.eclipse.che.plugin.svn.ide.commit;

import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.commit.CommitView.ActionDelegate;
import org.eclipse.che.plugin.svn.ide.commit.diff.DiffViewerPresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionOutputConsolePresenter;
import org.eclipse.che.plugin.svn.ide.common.SubversionActionPresenter;
import org.eclipse.che.plugin.svn.shared.CLIOutputParser;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;
import org.eclipse.che.plugin.svn.shared.StatusItem;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for the {@link CommitView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class CommitPresenter extends SubversionActionPresenter implements ActionDelegate {

    private final SubversionClientService                  subversionService;
    private final CommitView                               view;
    private       DiffViewerPresenter                      diffViewerPresenter;
    private final DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;

    private enum Changes {
        ALL,
        SELECTION
    }

    private Map<Changes, List<StatusItem>> cache = new HashMap<>();

    @Inject
    public CommitPresenter(final AppContext appContext,
                           final CommitView view,
                           final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                           final EventBus eventBus,
                           final NotificationManager notificationManager,
                           final SubversionOutputConsolePresenter console,
                           final SubversionExtensionLocalizationConstants constants,
                           final SubversionClientService subversionService,
                           final WorkspaceAgent workspaceAgent,
                           final ProjectExplorerPresenter projectExplorerPart,
                           final DiffViewerPresenter diffViewerPresenter) {
        super(appContext, eventBus, console, workspaceAgent, projectExplorerPart);
        this.subversionService = subversionService;
        this.view = view;
        this.diffViewerPresenter = diffViewerPresenter;
        this.view.setDelegate(this);
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.constants = constants;
    }

    public void show() {
        loadAllChanges();
    }

    private void loadAllChanges() {
        Unmarshallable<CLIOutputResponse> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class);
        subversionService.status(getCurrentProjectPath(), Collections.<String>emptyList(), null, false, false, false, true, false, null,
                                 new AsyncRequestCallback<CLIOutputResponse>(unmarshaller) {
                                     @Override
                                     protected void onSuccess(CLIOutputResponse response) {
                                         List<StatusItem> statusItems = parseChangesList(response);
                                         view.setChangesList(statusItems);
                                         view.onShow();

                                         cache.put(Changes.ALL, statusItems);
                                     }

                                     @Override
                                     protected void onFailure(Throwable exception) {
                                         Log.error(CommitPresenter.class, exception);
                                     }
                                 });
    }

    private  List<StatusItem> parseChangesList(CLIOutputResponse response) {
        return CLIOutputParser.parseFilesStatus(response.getOutput());
    }

    private void loadSelectionChanges() {
        Unmarshallable<CLIOutputResponse> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class);
        subversionService.status(getCurrentProjectPath(), getSelectedPaths(), null, false, false, false, true, false, null,
                                 new AsyncRequestCallback<CLIOutputResponse>(unmarshaller) {
                                     @Override
                                     protected void onSuccess(CLIOutputResponse response) {
                                         List<StatusItem> statusItems = parseChangesList(response);
                                         view.setChangesList(statusItems);

                                         cache.put(Changes.SELECTION, statusItems);
                                     }

                                     @Override
                                     protected void onFailure(Throwable exception) {
                                         Log.error(CommitPresenter.class, exception);
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
    public void showDiff(String path) {
        this.subversionService.showDiff(getCurrentProjectPath(), Collections.singletonList(path), "HEAD",
                                        new AsyncRequestCallback<CLIOutputResponse>(
                                                dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class)) {
                                            @Override
                                            protected void onSuccess(CLIOutputResponse result) {
                                                String content = Joiner.on('\n').join(result.getOutput());
                                                diffViewerPresenter.showDiff(content);
                                            }

                                            @Override
                                            protected void onFailure(Throwable exception) {
                                                notificationManager.notify(exception.getMessage(), FAIL, true);
                                            }
                                        });
    }

    private void commitSelection(final String message, final boolean keepLocks) {
        final List<String> paths = getSelectedPaths();
        doCommit(message, paths, keepLocks);
    }

    private void commitAll(final String message, final boolean keepLocks) {
        doCommit(message, Collections.singletonList("."), keepLocks);
    }

    private void doCommit(final String message, final List<String> paths, final boolean keepLocks) {
        this.subversionService.commit(getCurrentProjectPath(), paths, message, false, keepLocks,
                                      new AsyncRequestCallback<CLIOutputWithRevisionResponse>(
                                              dtoUnmarshallerFactory.newUnmarshaller(CLIOutputWithRevisionResponse.class)) {
                                          @Override
                                          protected void onSuccess(final CLIOutputWithRevisionResponse result) {
                                              printResponse(result.getCommand(), result.getOutput(), result.getErrOutput());
                                          }

                                          @Override
                                          protected void onFailure(final Throwable exception) {
                                              handleError(exception);
                                          }
                                      }
                                     );
        view.onClose();
    }

    private void handleError(@NotNull final Throwable e) {
        String errorMessage;
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            errorMessage = e.getMessage();
        } else {
            errorMessage = constants.commitFailed();
        }
        final StatusNotification notification = new StatusNotification(errorMessage, FAIL, true);
        this.notificationManager.notify(notification);
    }

}
