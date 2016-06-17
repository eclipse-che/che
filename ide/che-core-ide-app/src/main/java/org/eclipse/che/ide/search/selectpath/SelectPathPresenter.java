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
package org.eclipse.che.ide.search.selectpath;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.search.FullTextSearchView;

/**
 * Presenter for choosing directory for searching.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectPathPresenter implements SelectPathView.ActionDelegate {
    private final SelectPathView           view;
    private final ProjectExplorerPresenter projectExplorerPresenter;

    private FullTextSearchView.ActionDelegate searcher;

    @Inject
    public SelectPathPresenter(SelectPathView view, ProjectExplorerPresenter projectExplorerPresenter) {
        this.view = view;
        this.projectExplorerPresenter = projectExplorerPresenter;
        this.view.setDelegate(this);
    }

    /**
     * Show tree view with all root nodes of the workspace.
     *
     * @param searcher
     *         delegate from the root widget of the full-text-search mechanism
     */
    public void show(FullTextSearchView.ActionDelegate searcher) {
        this.searcher = searcher;
        view.setStructure(projectExplorerPresenter.getRootNodes());
        view.show();
    }

    /** {@inheritDoc} */
    @Override
    public void setSelectedPath(String path) {
        searcher.setPathDirectory(path);
        searcher.setFocus();
    }
}
