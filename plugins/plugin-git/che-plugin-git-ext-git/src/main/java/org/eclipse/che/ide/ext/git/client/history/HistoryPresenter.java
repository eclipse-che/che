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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.providers.DynaObject;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.shared.DiffType.RAW;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
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
@DynaObject
public class HistoryPresenter extends BasePresenter implements HistoryView.ActionDelegate {
    public static final String LOG_COMMAND_NAME  = "Git log";
    public static final String DIFF_COMMAND_NAME = "Git diff";

    private final HistoryView             view;
    private final GitServiceClient        service;
    private final GitLocalizationConstant constant;
    private final GitResources            resources;
    private final AppContext              appContext;
    private final DialogFactory           dialogFactory;
    private final WorkspaceAgent          workspaceAgent;
    private final DateTimeFormatter       dateTimeFormatter;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ProcessesPanelPresenter consolesPanelPresenter;
    /** If <code>true</code> then show all changes in project, if <code>false</code> then show changes of the selected resource. */
    private       boolean                 showChangesInProject;
    private       DiffWith                diffType;
    private boolean isViewClosed = true;
    private List<Revision>      revisions;
    private Revision            selectedRevision;
    private NotificationManager notificationManager;

    private Project project;

    @Inject
    public HistoryPresenter(HistoryView view,
                            GitResources resources,
                            GitServiceClient service,
                            WorkspaceAgent workspaceAgent,
                            GitLocalizationConstant constant,
                            AppContext appContext,
                            NotificationManager notificationManager,
                            DialogFactory dialogFactory,
                            DateTimeFormatter dateTimeFormatter,
                            GitOutputConsoleFactory gitOutputConsoleFactory,
                            ProcessesPanelPresenter processesPanelPresenter) {
        this.view = view;
        this.dialogFactory = dialogFactory;
        this.dateTimeFormatter = dateTimeFormatter;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.view.setDelegate(this);
        this.view.setTitle(constant.historyTitle());
        this.resources = resources;
        this.service = service;
        this.workspaceAgent = workspaceAgent;
        this.constant = constant;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
    }

    public void showDialog(Project project) {
        this.project = project;
        getCommitsLog();
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
    private void getCommitsLog() {
        service.log(appContext.getDevMachine(), project.getLocation(), null, false).then(new Operation<LogResponse>() {
            @Override
            public void apply(LogResponse log) throws OperationException {
                revisions = log.getCommits();
                view.setRevisions(revisions);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                if (getErrorCode(error.getCause()) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                    dialogFactory.createMessageDialog(constant.historyTitle(), constant.initCommitWasNotPerformed(), null).show();
                } else {
                    nothingToDisplay(null);
                    String errorMessage = error.getMessage() != null ? error.getMessage() : constant.logFailed();
                    GitOutputConsole console = gitOutputConsoleFactory.create(LOG_COMMAND_NAME);
                    console.printError(errorMessage);
                    consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                    notificationManager.notify(constant.logFailed(), FAIL, FLOAT_MODE);
                }
                partStack.minimize();
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
        getCommitsLog();
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
        getCommitsLog();
    }

    /** Get the changes between revisions. On success - display diff in text format, otherwise - show the error message in output panel. */
    private void getDiff() {
        Path path = Path.EMPTY;

        if (!showChangesInProject) {
            final Resource resource = appContext.getResource();

            if (resource != null && project.getLocation().isPrefixOf(resource.getLocation())) {
                path = resource.getLocation().removeFirstSegments(project.getLocation().segmentCount());
            }
        }

        if (DiffWith.DIFF_WITH_INDEX.equals(diffType) || DiffWith.DIFF_WITH_WORK_TREE.equals(diffType)) {
            boolean isCached = DiffWith.DIFF_WITH_INDEX.equals(diffType);
            doDiffWithNotCommitted(path.isEmpty() ? Collections.<String>emptyList()
                                                  : singletonList(path.toString()), selectedRevision, isCached);
        } else {
            doDiffWithPrevVersion(path.isEmpty() ? Collections.<String>emptyList()
                                                 : singletonList(path.toString()), selectedRevision);
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

        service.diff(appContext.getDevMachine(), project.getLocation(), filePatterns, RAW, false, 0, revision.getId(), isCached)
               .then(new Operation<String>() {
                   @Override
                   public void apply(String diff) throws OperationException {
                       view.setDiffContext(diff);
                       String text = isCached ? constant.historyDiffIndexState() : constant.historyDiffTreeState();
                       displayCommitA(revision);
                       view.setCompareType(text);
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError error) throws OperationException {
                       nothingToDisplay(revision);
                       String errorMessage = error.getMessage() != null ? error.getMessage() : constant.diffFailed();
                       GitOutputConsole console = gitOutputConsoleFactory.create(DIFF_COMMAND_NAME);
                       console.printError(errorMessage);
                       consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                       notificationManager.notify(constant.diffFailed(), FAIL, FLOAT_MODE);
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
            service.diff(appContext.getDevMachine(), project.getLocation(), filePatterns, RAW, false, 0, revisionA.getId(), revisionB.getId())
                   .then(new Operation<String>() {
                       @Override
                       public void apply(String diff) throws OperationException {
                           view.setDiffContext(diff);
                           view.setCompareType("");
                           displayCommitA(revisionA);
                           displayCommitB(revisionB);
                       }
                   })
                   .catchError(new Operation<PromiseError>() {
                       @Override
                       public void apply(PromiseError error) throws OperationException {
                           nothingToDisplay(revisionB);
                           String errorMessage = error.getMessage() != null ? error.getMessage() : constant.diffFailed();
                           GitOutputConsole console = gitOutputConsoleFactory.create(DIFF_COMMAND_NAME);
                           console.printError(errorMessage);
                           consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                           notificationManager.notify(constant.diffFailed(), FAIL, FLOAT_MODE);
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
    public SVGResource getTitleImage() {
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
    public IsWidget getView() {
        return view;
    }
}
