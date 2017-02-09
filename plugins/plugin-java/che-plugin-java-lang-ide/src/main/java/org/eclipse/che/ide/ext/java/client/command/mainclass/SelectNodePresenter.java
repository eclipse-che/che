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
package org.eclipse.che.ide.ext.java.client.command.mainclass;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.command.JavaCommandPagePresenter;
import org.eclipse.che.ide.resources.tree.ResourceNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter for choosing Main class.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectNodePresenter implements SelectNodeView.ActionDelegate {

    private final SelectNodeView           view;
    private final ResourceNode.NodeFactory nodeFactory;
    private final SettingsProvider         settingsProvider;
    private final AppContext               appContext;

    private JavaCommandPagePresenter delegate;

    @Inject
    public SelectNodePresenter(SelectNodeView view,
                               ResourceNode.NodeFactory nodeFactory,
                               SettingsProvider settingsProvider,
                               AppContext appContext) {
        this.view = view;
        this.nodeFactory = nodeFactory;
        this.settingsProvider = settingsProvider;
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

        final List<Node> nodes = new ArrayList<>();
        for (Project project : appContext.getProjects()) {
            nodes.add(nodeFactory.newContainerNode(project, settingsProvider.getSettings()));
        }

        view.setStructure(nodes);

        view.show();
    }

    @Override
    public void setSelectedNode(Resource resource, String fqn) {
        delegate.setMainClass(resource, fqn);
    }
}
