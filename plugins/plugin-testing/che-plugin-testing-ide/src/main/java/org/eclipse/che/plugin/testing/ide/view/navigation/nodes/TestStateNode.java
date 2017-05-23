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
package org.eclipse.che.plugin.testing.ide.view.navigation.nodes;

import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.plugin.testing.ide.model.TestState;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TestStateNode extends AbstractTreeNode implements HasPresentation {

    private NodePresentation nodePresentation;

    private final PromiseProvider promiseProvider;
    private final TestState testState;

    @Inject
    public TestStateNode(PromiseProvider promiseProvider, @Assisted TestState testState) {
        this.promiseProvider = promiseProvider;
        this.testState = testState;
    }

    @Override
    public String getName() {
        return testState.getPresentation();
    }

    @Override
    public boolean isLeaf() {
        return testState.isLeaf();
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        List<Node> child = new ArrayList<>();
        for (TestState state : testState.getChildren()) {
            child.add(new TestStateNode(promiseProvider, state));
        }
        return promiseProvider.resolve(child);
    }

    public TestState getTestState() {
        return testState;
    }

    @Override
    public void updatePresentation(NodePresentation presentation) {
        presentation.setPresentableText(testState.getPresentation());
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
}
