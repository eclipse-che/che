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
package org.eclipse.che.ide.ext.git.client.compare.changedpanel;

import com.google.inject.Inject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;

import java.util.Map;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for displaying list of changed files.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 */
public class ChangedPanelPresenter implements ChangedPanelView.ActionDelegate {
    private final ChangedPanelView        view;
    private final AppContext              appContext;
    private final NotificationManager     notificationManager;
    private final ComparePresenter        comparePresenter;
    private final GitLocalizationConstant locale;

    private Map<String, Status> changedFiles;
    private CallBack            callBack;
    private boolean             treeViewEnabled;

    @Inject
    public ChangedPanelPresenter(GitLocalizationConstant locale,
                                 ChangedPanelView view,
                                 AppContext appContext,
                                 NotificationManager notificationManager,
                                 ComparePresenter comparePresenter) {
        this.locale = locale;
        this.view = view;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.comparePresenter = comparePresenter;
        this.view.setDelegate(this);
        this.treeViewEnabled = true;
    }

    /**
     * Show panel with changed files. If empty map with changed files is received, all buttons would be disabled.
     *
     * @param changedFiles
     *         Map with files and their status
     */
    public void show(Map<String, Status> changedFiles, @Nullable CallBack callBack) {
        this.changedFiles = changedFiles;
        this.callBack = callBack;
        if (changedFiles.isEmpty()) {
            view.setTextToChangeViewModeButton(locale.changeListRowListViewButtonText());
            view.setEnabledChangeViewModeButton(false);
            view.setEnableExpandCollapseButtons(false);
            view.clearNodeStorage();
        } else {
            view.setEnabledChangeViewModeButton(true);
            view.setEnableExpandCollapseButtons(treeViewEnabled);
            viewChangedFiles();
        }
    }

    public ChangedPanelView getView() {
        return view;
    }

    @Override
    public void onFileNodeDoubleClicked(String path, final Status status) {
        appContext.getRootProject()
                  .getFile(path)
                  .then(file -> {
                      if (file.isPresent()) {
                          comparePresenter.showCompareWithLatest(file.get(), status, "HEAD");
                      }
                  })
                  .catchError(error -> {
                      notificationManager.notify(error.getMessage(), FAIL, NOT_EMERGE_MODE);
                  });
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
    public void onNodeSelected(Node node) {
        if (callBack != null) {
            callBack.onNodeSelected(node);
        }
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
}
