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
package org.eclipse.che.ide.search.presentation;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.data.tree.NodeInterceptor;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.search.factory.FindResultNodeFactory;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent;

import java.util.Collections;

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

        NodeStorage nodeStorage = new NodeStorage();
        NodeLoader loader = new NodeLoader(Collections.<NodeInterceptor>emptySet());
        tree = new Tree(nodeStorage, loader);

        tree.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                delegate.onSelectionChanged(event.getSelection());
            }
        });

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
    public void showResults(Resource[] resources, String request) {
        tree.getNodeStorage().clear();
        tree.getNodeStorage().add(findResultNodeFactory.newResultNode(resources, request));
        tree.expandAll();
        tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
        focusView();
    }

    @Override
    public Tree getTree() {
        return tree;
    }
}
