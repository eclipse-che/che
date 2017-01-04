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
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;

import javax.validation.constraints.NotNull;

import java.util.Map;

/**
 * Presenter for displaying list of changed files.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ChangedListPresenter implements ChangedListView.ActionDelegate {
    private final ChangedListView         view;
    private final GitLocalizationConstant locale;
    private final ComparePresenter        comparePresenter;

    private Map<String, Status> changedFiles;
    private Project             project;
    private String              file;
    private String              revision;
    private Status              status;
    private boolean             treeViewEnabled;

    @Inject
    public ChangedListPresenter(ChangedListView view,
                                ComparePresenter comparePresenter,
                                GitLocalizationConstant locale) {
        this.comparePresenter = comparePresenter;
        this.view = view;
        this.locale = locale;
        this.view.setDelegate(this);
    }

    /**
     * Show window with changed files.
     *
     * @param changedFiles
     *         Map with files and their status
     * @param revision
     *         hash of revision or branch
     */
    public void show(Map<String, Status> changedFiles, String revision, Project project) {
        this.changedFiles = changedFiles;
        this.project = project;
        this.revision = revision;

        view.setEnableCompareButton(false);
        view.setEnableExpandCollapseButtons(treeViewEnabled);

        view.showDialog();
        viewChangedFiles();
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onCompareClicked() {
        showCompare();
    }

    @Override
    public void onFileNodeDoubleClicked() {
        showCompare();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
        project.getFile(file).then(new Operation<Optional<File>>() {
            @Override
            public void apply(Optional<File> file) throws OperationException {
                if (file.isPresent()) {
                    comparePresenter.show(file.get(), status, revision);
                }
            }
        });
    }
}
