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
package org.eclipse.che.ide.ext.git.client.history;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectEvent;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.api.git.shared.DiffRequest.DiffType.RAW;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter for showing git history.
 * This presenter must implements ActivePartChangedHandler and PropertyListener to be able to get the changes for the selected resource
 * and change a dedicated resource with the history-window open
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class HistoryPresenter extends BasePresenter implements HistoryView.ActionDelegate {
    public static final String LOG_COMMAND_NAME  = "Git log";
    public static final String DIFF_COMMAND_NAME = "Git diff";

    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final HistoryView             view;
    private final GitServiceClient        service;
    private final GitLocalizationConstant constant;
    private final GitResources            resources;
    private final AppContext              appContext;
    private final DialogFactory           dialogFactory;    
    private final WorkspaceAgent          workspaceAgent;
    private final DateTimeFormatter       dateTimeFormatter;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ConsolesPanelPresenter  consolesPanelPresenter;
    private final String                  workspaceId;
    /** If <code>true</code> then show all changes in project, if <code>false</code> then show changes of the selected resource. */
    private       boolean                 showChangesInProject;
    private       DiffWith                diffType;
    private boolean isViewClosed = true;
    private List<Revision>      revisions;
    private SelectionAgent      selectionAgent;
    private Revision            selectedRevision;
    private NotificationManager notificationManager;

    @Inject
    public HistoryPresenter(final HistoryView view,
                            EventBus eventBus,
                            GitResources resources,
                            GitServiceClient service,
                            final WorkspaceAgent workspaceAgent,
                            GitLocalizationConstant constant,
                            AppContext appContext,
                            NotificationManager notificationManager,
                            DialogFactory dialogFactory,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            DateTimeFormatter dateTimeFormatter,
                            SelectionAgent selectionAgent,
                            GitOutputConsoleFactory gitOutputConsoleFactory,
                            ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.dialogFactory = dialogFactory;
        this.dateTimeFormatter = dateTimeFormatter;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.view.setDelegate(this);
        this.view.setTitle(constant.historyTitle());
        this.resources = resources;
        this.service = service;
        this.workspaceAgent = workspaceAgent;
        this.constant = constant;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.selectionAgent = selectionAgent;
        this.workspaceId = appContext.getWorkspaceId();

        eventBus.addHandler(CloseCurrentProjectEvent.TYPE, new CloseCurrentProjectHandler() {
            @Override
            public void onCloseCurrentProject(CloseCurrentProjectEvent event) {
                isViewClosed = true;
                workspaceAgent.hidePart(HistoryPresenter.this);
                view.clear();
            }
        });
    }

    /** Show dialog. */
    public void showDialog() {
        ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        getCommitsLog(project);
        selectedRevision = null;

        view.selectProjectChangesButton(true);
        view.selectResourceChangesButton(false);
        showChangesInProject = true;
        view.selectDiffWithPrevVersionButton(true);
        diffType = DiffWith.DIFF_WITH_PREV_VERSION;

        displayCommitA(null);
        displayCommitB(null);
        view.setDiffContext("");
        view.setCompareType(constant.historyNothingToDisplay());

        if (isViewClosed) {
            workspaceAgent.openPart(this, PartStackType.TOOLING);
            isViewClosed = false;
        }

        PartPresenter activePart = partStack.getActivePart();
        if (activePart == null || !activePart.equals(this)) {
            partStack.setActivePart(this);
        }
    }

    /** Get the log of the commits. If successfully received, then display in revision grid, otherwise - show error in output panel. */
    private void getCommitsLog(final ProjectConfigDto project) {
        service.log(workspaceId, project, null, false,
                    new AsyncRequestCallback<LogResponse>(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class)) {
                        @Override
                        protected void onSuccess(LogResponse result) {
                            revisions = result.getCommits();
                            view.setRevisions(revisions);
                        }

                        @Override
                        protected void onFailure(Throwable exception) {
                            if (getErrorCode(exception) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                                dialogFactory.createMessageDialog(constant.historyTitle(),
                                                                  constant.initCommitWasNotPerformed(),
                                                                  null).show();
                            } else {
                                nothingToDisplay(null);
                                String errorMessage = exception.getMessage() != null ? exception.getMessage() : constant.logFailed();
                                GitOutputConsole console = gitOutputConsoleFactory.create(LOG_COMMAND_NAME);
                                console.printError(errorMessage);
                                consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                notificationManager.notify(constant.logFailed(), FAIL, true, project);
                            }
                            partStack.hidePart(HistoryPresenter.this);
                            workspaceAgent.removePart(HistoryPresenter.this);
                            isViewClosed = true;
                        }
                    });
    }

    /**
     * Clear the comparance result, when there is nothing to compare.
     *
     * @param revision
     */
    private void nothingToDisplay(@Nullable Revision revision) {
        displayCommitA(revision);
        displayCommitB(null);
        view.setCompareType(constant.historyNothingToDisplay());
        view.setDiffContext("");
    }

    /**
     * Display information about commit A.
     *
     * @param revision
     *         revision what need to display
     */
    private void displayCommitA(@Nullable Revision revision) {
        if (revision == null) {
            view.setCommitADate("");
            view.setCommitARevision("");
        } else {
            view.setCommitADate(dateTimeFormatter.getFormattedDate(revision.getCommitTime()));
            view.setCommitARevision(revision.getId());
        }
    }

    /**
     * Display information about commit B.
     *
     * @param revision
     *         revision what need to display
     */
    private void displayCommitB(@Nullable Revision revision) {
        boolean isEmpty = revision == null;
        if (isEmpty) {
            view.setCommitBDate("");
            view.setCommitBRevision("");
        } else {
            view.setCommitBDate(dateTimeFormatter.getFormattedDate(revision.getCommitTime()));
            view.setCommitBRevision(revision.getId());
        }
        view.setCommitBPanelVisible(!isEmpty);
    }

    /** {@inheritDoc} */
    @Override
    public void onRefreshClicked() {
        ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        getCommitsLog(project);
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectChangesClicked() {
        if (showChangesInProject) {
            return;
        }
        showChangesInProject = true;
        view.selectProjectChangesButton(true);
        view.selectResourceChangesButton(false);
        update();
    }

    /** {@inheritDoc} */
    @Override
    public void onResourceChangesClicked() {
        if (!showChangesInProject) {
            return;
        }
        showChangesInProject = false;
        view.selectProjectChangesButton(false);
        view.selectResourceChangesButton(true);
        update();
    }

    /** {@inheritDoc} */
    @Override
    public void onDiffWithIndexClicked() {
        if (DiffWith.DIFF_WITH_INDEX.equals(diffType)) {
            return;
        }
        diffType = DiffWith.DIFF_WITH_INDEX;
        view.selectDiffWithIndexButton(true);
        view.selectDiffWithPrevVersionButton(false);
        view.selectDiffWithWorkingTreeButton(false);
        update();
    }

    /** {@inheritDoc} */
    @Override
    public void onDiffWithWorkTreeClicked() {
        if (DiffWith.DIFF_WITH_WORK_TREE.equals(diffType)) {
            return;
        }
        diffType = DiffWith.DIFF_WITH_WORK_TREE;
        view.selectDiffWithIndexButton(false);
        view.selectDiffWithPrevVersionButton(false);
        view.selectDiffWithWorkingTreeButton(true);
        update();
    }

    /** {@inheritDoc} */
    @Override
    public void onDiffWithPrevCommitClicked() {
        if (DiffWith.DIFF_WITH_PREV_VERSION.equals(diffType)) {
            return;
        }
        diffType = DiffWith.DIFF_WITH_PREV_VERSION;
        view.selectDiffWithIndexButton(false);
        view.selectDiffWithPrevVersionButton(true);
        view.selectDiffWithWorkingTreeButton(false);
        update();
    }

    /** {@inheritDoc} */
    @Override
    public void onRevisionSelected(@NotNull Revision revision) {
        selectedRevision = revision;
        update();
    }

    /** Update content. */
    private void update() {
        getDiff();
        ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        getCommitsLog(project);
    }

    /** Get the changes between revisions. On success - display diff in text format, otherwise - show the error message in output panel. */
    private void getDiff() {
        String pattern = "";
        ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        if (!showChangesInProject && project != null) {
            String path;

            Selection<HasStorablePath> selection = (Selection<HasStorablePath>)selectionAgent.getSelection();

            if (selection == null || selection.getHeadElement() == null) {
                path = project.getPath();
            } else {
                path = selection.getHeadElement().getStorablePath();
            }

            pattern = path.replaceFirst(project.getPath(), "");
            pattern = (pattern.startsWith("/")) ? pattern.replaceFirst("/", "") : pattern;
        }

        if (DiffWith.DIFF_WITH_INDEX.equals(diffType) || DiffWith.DIFF_WITH_WORK_TREE.equals(diffType)) {
            boolean isCached = DiffWith.DIFF_WITH_INDEX.equals(diffType);
            doDiffWithNotCommitted((pattern.length() > 0) ? new ArrayList<>(Arrays.asList(pattern)) : new ArrayList<String>(),
                                   selectedRevision, isCached);
        } else {
            doDiffWithPrevVersion((pattern.length() > 0) ? new ArrayList<>(Arrays.asList(pattern)) : new ArrayList<String>(),
                                  selectedRevision);
        }
    }

    /**
     * Perform diff between pointed revision and index or working tree.
     *
     * @param filePatterns
     *         patterns for which to show diff
     * @param revision
     *         revision to compare with
     * @param isCached
     *         if <code>true</code> compare with index, else - with working tree
     */
    private void doDiffWithNotCommitted(@NotNull List<String> filePatterns, @Nullable final Revision revision, final boolean isCached) {
        if (revision == null) {
            return;
        }

        final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        service.diff(workspaceId, project, filePatterns, RAW, false, 0, revision.getId(), isCached,
                     new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                         @Override
                         protected void onSuccess(String result) {
                             view.setDiffContext(result);
                             String text = isCached ? constant.historyDiffIndexState() : constant.historyDiffTreeState();
                             displayCommitA(revision);
                             view.setCompareType(text);
                         }

                         @Override
                         protected void onFailure(Throwable exception) {
                             nothingToDisplay(revision);
                             String errorMessage = exception.getMessage() != null ? exception.getMessage() : constant.diffFailed();
                             GitOutputConsole console = gitOutputConsoleFactory.create(DIFF_COMMAND_NAME);
                             console.printError(errorMessage);
                             consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                             notificationManager.notify(constant.diffFailed(), FAIL, true, project);
                         }
                     });
    }

    /**
     * Perform diff between selected commit and previous one.
     *
     * @param filePatterns
     *         patterns for which to show diff
     * @param revisionB
     *         selected commit
     */
    private void doDiffWithPrevVersion(@NotNull List<String> filePatterns, @Nullable final Revision revisionB) {
        if (revisionB == null) {
            return;
        }

        int index = revisions.indexOf(revisionB);
        if (index + 1 < revisions.size()) {
            final Revision revisionA = revisions.get(index + 1);
            final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
            service.diff(workspaceId, project, filePatterns, RAW, false, 0, revisionA.getId(),
                         revisionB.getId(),
                         new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                             @Override
                             protected void onSuccess(String result) {
                                 view.setDiffContext(result);
                                 view.setCompareType("");
                                 displayCommitA(revisionA);
                                 displayCommitB(revisionB);
                             }

                             @Override
                             protected void onFailure(Throwable exception) {
                                 nothingToDisplay(revisionB);
                                 String errorMessage = exception.getMessage() != null ? exception.getMessage() : constant.diffFailed();
                                 GitOutputConsole console = gitOutputConsoleFactory.create(DIFF_COMMAND_NAME);
                                 console.printError(errorMessage);
                                 consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                 notificationManager.notify(constant.diffFailed(), FAIL, true, project);
                             }
                         });
        } else {
            nothingToDisplay(revisionB);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return constant.historyTitle();
    }

    /** {@inheritDoc} */
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SVGResource getTitleSVGImage() {
        return resources.history();
    }

    /** {@inheritDoc} */
    @Override
    public String getTitleToolTip() {
        return constant.historyTitle();
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return 450;
    }

    protected enum DiffWith {
        DIFF_WITH_INDEX,
        DIFF_WITH_WORK_TREE,
        DIFF_WITH_PREV_VERSION
    }

    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Override
    public IsWidget getView() {
        return view;
    }
}
