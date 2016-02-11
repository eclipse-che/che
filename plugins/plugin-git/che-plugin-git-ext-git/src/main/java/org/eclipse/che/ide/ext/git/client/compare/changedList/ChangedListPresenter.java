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
package org.eclipse.che.ide.ext.git.client.compare.changedList;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;

import javax.validation.constraints.NotNull;

import java.util.Map;

/**
 * Presenter for displaying list of changed files.
 *
 * @author Igor Vinokur
 */
@Singleton
public class ChangedListPresenter implements ChangedListView.ActionDelegate {
    private final ChangedListView  view;
    private final ComparePresenter comparePresenter;

    private String file;
    private String state;
    private String revision;

    @Inject
    public ChangedListPresenter(ChangedListView view,
                                ComparePresenter comparePresenter) {
        this.comparePresenter = comparePresenter;
        this.view = view;
        this.view.setDelegate(this);
    }

    /**
     * Show window with changed files list.
     *
     * @param changedFiles
     *         Map with files and their state
     * @param revision
     *         hash of revision or branch
     */
    public void show(Map<String, String> changedFiles, String revision) {
        view.showDialog();
        view.setChanges(changedFiles);
        view.setEnableCompareButton(false);
        this.revision = revision;
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onCompareClicked() {
        comparePresenter.show(file, state, revision);
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeSelected(@NotNull Node node) {
        view.setEnableCompareButton(true);
        this.file = node.getName();
        this.state = ((ChangedNode)node).getState();
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeUnselected() {
        view.setEnableCompareButton(false);
    }
}