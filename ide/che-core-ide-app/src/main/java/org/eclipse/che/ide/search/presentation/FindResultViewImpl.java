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
package org.eclipse.che.ide.search.presentation;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.search.factory.FindResultNodeFactory;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.UniqueKeyProvider;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * Implementation for FindResult view.
 * Uses tree for presenting search results.
 *
 * @author Valeriy Svydenko
 */
@Singleton
class FindResultViewImpl extends BaseView<FindResultView.ActionDelegate> implements FindResultView {
    private final Tree                  tree;
    private final FindResultNodeFactory findResultNodeFactory;

    @Inject
    public FindResultViewImpl(PartStackUIResources resources,
                              FindResultNodeFactory findResultNodeFactory,
                              CoreLocalizationConstant localizationConstant) {
        super(resources);
        setTitle(localizationConstant.actionFullTextSearch());
        this.findResultNodeFactory = findResultNodeFactory;

        UniqueKeyProvider<Node> nodeIdProvider = new NodeUniqueKeyProvider() {
            @NotNull
            @Override
            public String getKey(@NotNull Node item) {
                if (item instanceof HasStorablePath) {
                    return ((HasStorablePath)item).getStorablePath();
                } else {
                    return String.valueOf(item.hashCode());
                }
            }
        };

        NodeStorage nodeStorage = new NodeStorage(nodeIdProvider);
        NodeLoader loader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
        tree = new Tree(nodeStorage, loader);

        setContentWidget(tree);

        tree.setAutoSelect(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void focusView() {
        tree.setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    public void showResults(List<ItemReference> nodes, String request) {
        tree.getNodeStorage().clear();
        tree.getNodeStorage().add(findResultNodeFactory.newResultNode(nodes, request));
        tree.expandAll();
        tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
        focusView();
    }
}
