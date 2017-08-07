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
import org.eclipse.che.ide.ext.git.client.compare.ChangedItems;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangedFolderNode;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelPresenter;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent.SelectionChangedHandler;

import static com.google.common.collect.Iterables.getFirst;

/**
 * Presenter for displaying window with list of changed files.
 *
 * @author Igor Vinokur
 * @author Vlad Zhukovskyi
 * @author Mykola Morhun
 */
@Singleton
public class ChangesListPresenter implements ChangesListView.ActionDelegate {
    private final ChangesListView       view;
    private final ChangesPanelPresenter changesPanelPresenter;
    private final ComparePresenter      comparePresenter;

    private ChangedItems changedItems;
    private String       file;
    private String       revisionA;
    private String       revisionB;

    @Inject
    public ChangesListPresenter(ChangesListView view,
                                ComparePresenter comparePresenter,
                                ChangesPanelPresenter changesPanelPresenter) {
        this.comparePresenter = comparePresenter;
        this.view = view;
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
        };

        ChangesPanelView changesPanelView = changesPanelPresenter.getView();
        changesPanelView.addSelectionHandler(handler);
        this.view.setChangesPanelView(changesPanelView);
    }

    /**
     * Shows window with changed files.
     *
     * @param changedItems
     *         files and their status
     * @param revisionA
     *         hash of the first revision or branch.
     *         If it is set to {@code null}, compare with empty repository state will be performed
     * @param revisionB
     *         hash of the second revision or branch.
     *         If it is set to {@code null}, compare with latest repository state will be performed
     */
    public void show(ChangedItems changedItems, @Nullable String revisionA, @Nullable String revisionB) {
        this.changedItems = changedItems;
        this.revisionA = revisionA;
        this.revisionB = revisionB;

        view.setEnableCompareButton(false);
        view.showDialog();

        changesPanelPresenter.show(changedItems);
    }

    @Override
    public void onCloseClicked() {
        view.close();
    }

    @Override
    public void onCompareClicked() {
        if (revisionB == null) {
            comparePresenter.showCompareWithLatest(changedItems, file, revisionA);
        } else {
            comparePresenter.showCompareBetweenRevisions(changedItems, file, revisionA, revisionB);
        }
    }
}
