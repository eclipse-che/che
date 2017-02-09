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
package org.eclipse.che.ide.ext.java.client.search;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.search.node.NodeFactory;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;

import javax.validation.constraints.NotNull;
import java.util.Collections;

/**
 * Implementation for FindUsages view.
 * Uses tree for presenting search results.
 *
 * @author Evgen Vidolob
 */
@Singleton
class FindUsagesViewImpl extends BaseView<FindUsagesView.ActionDelegate> implements FindUsagesView {

    private final Tree        tree;
    private final NodeFactory nodeFactory;

    @Inject
    public FindUsagesViewImpl(PartStackUIResources resources, NodeFactory nodeFactory, JavaLocalizationConstant localizationConstant) {
        super(resources);
        this.nodeFactory = nodeFactory;
        setTitle(localizationConstant.findUsagesPartTitle());
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
        panel.ensureDebugId("findUsages-panel");
    }

    @Override
    protected void focusView() {
        tree.setFocus(true);
    }

    @Override
    public void showUsages(final FindUsagesResponse usagesResponse) {
        tree.getNodeStorage().clear();
        tree.getNodeStorage().add(nodeFactory.create(usagesResponse));

        tree.expandAll();

        if (!tree.getRootNodes().isEmpty()) {
            tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
        }
    }
}
