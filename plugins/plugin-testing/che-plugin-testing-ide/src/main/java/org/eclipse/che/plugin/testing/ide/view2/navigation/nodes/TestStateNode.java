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
package org.eclipse.che.plugin.testing.ide.view2.navigation.nodes;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.plugin.testing.ide.TestResources;
import org.eclipse.che.plugin.testing.ide.model.TestState;
import org.eclipse.che.plugin.testing.ide.model.info.TestStateDescription;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes UI state of the test node.
 */
public class TestStateNode extends AbstractTreeNode implements HasPresentation {

    private final PromiseProvider  promiseProvider;
    private final TestResources    testResources;
    private final TestState        testState;

    private       NodePresentation nodePresentation;

    @Inject
    public TestStateNode(PromiseProvider promiseProvider,
                         TestResources testResources,
                         @Assisted TestState testState) {
        this.promiseProvider = promiseProvider;
        this.testResources = testResources;
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
            if (!state.isConfig() || !state.isPassed()) {
                child.add(new TestStateNode(promiseProvider, testResources, state));
            }
        }
        return promiseProvider.resolve(child);
    }

    public TestState getTestState() {
        return testState;
    }

    @Override
    public void updatePresentation(NodePresentation presentation) {
        presentation.setPresentableText(testState.getPresentation());
        if (testState.isSuite()) {
            return;
        }
        if (testState.getDescription() == TestStateDescription.PASSED) {
            presentation.setPresentableTextCss("color: green;");
            presentation.setPresentableIcon(testResources.testResultSuccessIcon());
        } else if (testState.getDescription() == TestStateDescription.IGNORED) {
            presentation.setPresentableTextCss("text-decoration: line-through; color: yellow;");
            presentation.setPresentableIcon(testResources.testResultSkippedIcon());
        } else if (testState.getDescription() == TestStateDescription.FAILED || testState.getDescription() == TestStateDescription.ERROR) {
            presentation.setPresentableTextCss("color: red;");
            presentation.setPresentableIcon(testResources.testResultFailureIcon());
        } else if (testState.getDescription() == TestStateDescription.RUNNING) {
            presentation.setPresentableIcon(testResources.testInProgressIcon());
        } else if (testState.getDescription() == TestStateDescription.NOT_RUN) {
            presentation.setPresentableIcon(testResources.testResultSkippedIcon());
        }
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
