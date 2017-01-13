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
package org.eclipse.che.plugin.languageserver.ide.location;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.inject.Inject;

import org.eclipse.che.api.languageserver.shared.lsapi.LocationDTO;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
public class OpenLocationViewImpl extends BaseView<OpenLocationView.ActionDelegate> implements OpenLocationView {

    private final Tree tree;

    @Inject
    public OpenLocationViewImpl(PartStackUIResources resources) {
        super(resources);
        DockLayoutPanel panel = new DockLayoutPanel(Style.Unit.PX);

        NodeStorage storage = new NodeStorage(new NodeUniqueKeyProvider() {
            @Override
            public String getKey(@NotNull Node item) {
                return String.valueOf(item.hashCode());
            }
        });
        NodeLoader loader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
        tree = new Tree(storage, loader);
        panel.add(tree);
        setContentWidget(panel);
        panel.ensureDebugId("LS-open-location-panel");
    }

    @Override
    public void setLocations(List<LocationDTO> locations) {
        tree.getNodeStorage().clear();
        //TODO workaround, tree has bug with adding list of nodes
        for (LocationDTO location : locations) {
            tree.getNodeStorage().add(new LocationNode(location));
        }

        tree.expandAll();

        if (!tree.getRootNodes().isEmpty()) {
            tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
        }
    }

    private class LocationNode extends AbstractTreeNode implements HasAction, HasPresentation {
        private final LocationDTO location;
        private NodePresentation nodePresentation;

        public LocationNode(LocationDTO location) {
            this.location = location;
        }

        @Override
        public void updatePresentation(@NotNull NodePresentation presentation) {
            presentation.setPresentableText(location.getUri());
            presentation.setInfoText(
                    "From:" + location.getRange().getStart().getLine() + ":" + location.getRange().getStart().getCharacter() + " To:" +
                    location.getRange().getEnd().getLine() + ":" + location.getRange().getEnd().getCharacter());
        }

        @Override
        public NodePresentation getPresentation(boolean update) {
            if (nodePresentation == null) {
                nodePresentation = new NodePresentation();
                updatePresentation(nodePresentation);
            }

            if (update) {
                updatePresentation(nodePresentation);
            }
            return nodePresentation;
        }

        @Override
        protected Promise<List<Node>> getChildrenImpl() {
            return null;
        }

        @Override
        public void actionPerformed() {
            delegate.onLocationSelected(location);
        }

        @Override
        public String getName() {
            return location.getUri();
        }

        @Override
        public boolean isLeaf() {
            return true;
        }
    }
}
