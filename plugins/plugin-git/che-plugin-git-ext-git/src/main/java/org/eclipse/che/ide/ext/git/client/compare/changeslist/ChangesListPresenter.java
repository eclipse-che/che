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
package org.eclipse.che.ide.ext.git.client.compare.changeslist;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangedFileNode;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangedFolderNode;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelPresenter;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent.SelectionChangedHandler;

import java.util.Map;

import static com.google.common.collect.Iterables.getFirst;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for displaying list of changed files.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ChangesListPresenter implements ChangesListView.ActionDelegate {
    private final ChangesListView       view;
    private final NotificationManager   notificationManager;
    private final ChangesPanelPresenter changesPanelPresenter;
    private final ComparePresenter      comparePresenter;

    private Project project;
    private String  file;
    private String  revisionA;
    private String  revisionB;
    private Status  status;

    @Inject
    public ChangesListPresenter(ChangesListView view,
                                ComparePresenter comparePresenter,
                                NotificationManager notificationManager,
                                ChangesPanelPresenter changesPanelPresenter) {
        this.comparePresenter = comparePresenter;
        this.view = view;
        this.notificationManager = notificationManager;
        this.changesPanelPresenter = changesPanelPresenter;
        this.changesPanelPresenter.setFileNodeDoubleClickHandler((path, status) -> this.onCompareClicked());
        this.view.setDelegate(this);

        SelectionChangedHandler handler = event -> {
            Node node = getFirst(event.getSelection(), null);
            if (node == null) {
                return;
            }
            if (node instanceof ChangedFolderNode) {
                ChangesListPresenter.this.view.setEnableCompareButton(false);
                return;
            }
            ChangesListPresenter.this.view.setEnableCompareButton(true);
            ChangesListPresenter.this.file = node.getName();
            ChangesListPresenter.this.status = ((ChangedFileNode)node).getStatus();
        };

        ChangesPanelView changesPanelView = changesPanelPresenter.getView();
        changesPanelView.addSelectionHandler(handler);
        this.view.setChangesPanelView(changesPanelView);
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
        this.project = project;
        this.revisionA = revisionA;
        this.revisionB = revisionB;

        view.setEnableCompareButton(false);
        view.showDialog();

        changesPanelPresenter.show(changedFiles);
    }

    @Override
    public void onCloseClicked() {
        view.close();
    }

    @Override
    public void onCompareClicked() {
        if (revisionB == null) {
            project.getFile(file)
                   .then(file -> {
                       if (file.isPresent()) {
                           comparePresenter.showCompareWithLatest(file.get(), status, revisionA);
                       }
                   })
                   .catchError(error -> {
                       notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                   });
        } else {
            comparePresenter.showCompareBetweenRevisions(Path.valueOf(file), status, revisionA, revisionB);
        }
    }
}
