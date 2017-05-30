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
package org.eclipse.che.ide.resources.selector;


import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Vlad Zhukovskiy
 */
@Singleton
public class SelectPathPresenter implements SelectPathView.ActionDelegate {

    private final SelectPathView view;
    private final ResourceNode.NodeFactory nodeFactory;
    private final SettingsProvider settingsProvider;

    private Path selectedPath = Path.ROOT;

    private SelectionPathHandler handler;

    @Inject
    public SelectPathPresenter(SelectPathView view,
                               ResourceNode.NodeFactory nodeFactory,
                               SettingsProvider settingsProvider) {
        this.view = view;
        this.nodeFactory = nodeFactory;
        this.settingsProvider = settingsProvider;

        this.view.setDelegate(this);
    }

    public void show(Resource[] resources, boolean showFiles, SelectionPathHandler handler) {
        checkArgument(resources != null, "Null resources occurred");

        if (resources.length == 0) {
            view.setStructure(Collections.<Node>emptyList(), showFiles);
            return;
        }

        final List<Node> nodes = new ArrayList<>();
        final NodeSettings settings = settingsProvider.getSettings();

        for (Resource resource : resources) {
            if (resource.getResourceType() == Resource.FILE && !showFiles) {
                continue;
            }

            final Node node;

            if (resource.getResourceType() == Resource.FILE) {
                node = nodeFactory.newFileNode((File)resource, settings);
            } else {
                node = nodeFactory.newContainerNode((Container)resource, settings);
            }

            nodes.add(node);
        }

        view.setStructure(nodes, showFiles);

        this.handler = handler;

        view.show();
    }

    @Override
    public void onPathSelected(Path path) {
        selectedPath = path;
    }

    @Override
    public void onSubmit() {
        if (handler != null) {
            handler.onPathSelected(selectedPath);
        }
    }

    @Override
    public void onCancel() {
        if (handler != null) {
            handler.onSelectionCancelled();
        }
    }
}
