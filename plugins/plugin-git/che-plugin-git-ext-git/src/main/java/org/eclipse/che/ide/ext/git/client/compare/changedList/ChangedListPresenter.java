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
package org.eclipse.che.ide.ext.git.client.compare.changedList;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;

import java.util.Map;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for displaying list of changed files.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ChangedListPresenter implements ChangedListView.ActionDelegate {
    private final ChangedListView         view;
    private final NotificationManager     notificationManager;
    private final GitLocalizationConstant locale;
    private final ComparePresenter        comparePresenter;

    private Map<String, Status> changedFiles;
    private Project             project;
    private String              file;
    private String              revisionA;
    private String              revisionB;
    private Status              status;
    private boolean             treeViewEnabled;

    @Inject
    public ChangedListPresenter(ChangedListView view,
                                ComparePresenter comparePresenter,
                                NotificationManager notificationManager,
                                GitLocalizationConstant locale) {
        this.comparePresenter = comparePresenter;
        this.view = view;
        this.notificationManager = notificationManager;
        this.locale = locale;
        this.view.setDelegate(this);
    }

    /**
     * Show window with changed files.
     *
     * @param changedFiles
     *         Map with files and their status
     * @param revisionA
     *         hash of the first revision or branch.
     *         If it is set to {@code null}, compare with empty repository state will be performed
     * @param revisionB
     *         hash of the second revision or branch.
     *         If it is set to {@code null}, compare with latest repository state will be performed
     */
    public void show(Map<String, Status> changedFiles, @Nullable String revisionA, @Nullable String revisionB, Project project) {
        this.changedFiles = changedFiles;
        this.project = project;
        this.revisionA = revisionA;
        this.revisionB = revisionB;

        view.setEnableCompareButton(false);
        view.setEnableExpandCollapseButtons(treeViewEnabled);

        view.showDialog();
        viewChangedFiles();
    }

    @Override
    public void onCloseClicked() {
        view.close();
    }

    @Override
    public void onCompareClicked() {
        showCompare();
    }

    @Override
    public void onFileNodeDoubleClicked() {
        showCompare();
    }

    @Override
    public void onChangeViewModeButtonClicked() {
        treeViewEnabled = !treeViewEnabled;
        viewChangedFiles();
        view.setEnableExpandCollapseButtons(treeViewEnabled);
    }

    @Override
    public void onExpandButtonClicked() {
        view.expandAllDirectories();
    }

    @Override
    public void onCollapseButtonClicked() {
        view.collapseAllDirectories();
    }

    @Override
    public void onNodeSelected(@NotNull Node node) {
        if (node instanceof ChangedFolderNode) {
            view.setEnableCompareButton(false);
            return;
        }
        view.setEnableCompareButton(true);
        this.file = node.getName();
        this.status = ((ChangedFileNode)node).getStatus();
    }

    private void viewChangedFiles() {
        if (treeViewEnabled) {
            view.viewChangedFilesAsTree(changedFiles);
            view.setTextToChangeViewModeButton(locale.changeListRowListViewButtonText());
        } else {
            view.viewChangedFilesAsList(changedFiles);
            view.setTextToChangeViewModeButton(locale.changeListGroupByDirectoryButtonText());
        }
    }

    private void showCompare() {
        if (revisionB == null) {
            project.getFile(file)
                   .then(new Operation<Optional<File>>() {
                       @Override
                       public void apply(Optional<File> file) throws OperationException {
                           if (file.isPresent()) {
                               comparePresenter.showCompareWithLatest(file.get(), status, revisionA);
                           }
                       }
                   })
                   .catchError(new Operation<PromiseError>() {
                       @Override
                       public void apply(PromiseError error) throws OperationException {
                           notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                       }
                   });
        } else {
            comparePresenter.showCompareBetweenRevisions(Path.valueOf(file), status, revisionA, revisionB);
        }
    }
}
