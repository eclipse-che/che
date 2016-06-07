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
package org.eclipse.che.ide.ext.java.client.command.mainclass;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.java.client.command.JavaCommandPagePresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

import java.util.Collections;

/**
 * Presenter for choosing Main class.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectNodePresenter implements SelectNodeView.ActionDelegate {

    private final SelectNodeView           view;
    private final ProjectExplorerPresenter projectExplorerPresenter;
    private final AppContext               appContext;

    private JavaCommandPagePresenter delegate;

    @Inject
    public SelectNodePresenter(SelectNodeView view,
                               ProjectExplorerPresenter projectExplorerPresenter,
                               AppContext appContext) {
        this.view = view;
        this.projectExplorerPresenter = projectExplorerPresenter;
        this.appContext = appContext;
        this.view.setDelegate(this);
    }

    /**
     * Show tree view with all needed nodes of the workspace.
     *
     * @param presenter
     *         delegate from the page
     */
    public void show(JavaCommandPagePresenter presenter) {
        this.delegate = presenter;
        for (Node node : projectExplorerPresenter.getRootNodes()) {
            if (node.getName().equals(appContext.getCurrentProject().getRootProject().getName())) {
                view.setStructure(Collections.singletonList(node));
                break;
            }
        }

        view.show();
    }

    @Override
    public void setSelectedNode(String path, String fqn) {
        delegate.setMainClass(path, fqn);
    }
}
