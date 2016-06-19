package org.eclipse.che.ide.ext.java.testing.junit4x.client.view.navigation.nodes;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.java.testing.junit4x.client.TestResources;
import org.eclipse.che.ide.ext.java.testing.junit4x.client.view.navigation.factory.TestResultNodeFactory;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.List;

public class TestResultGroupNode extends AbstractTreeNode implements HasPresentation {

    int failureCount;
    private NodePresentation nodePresentation;
    private final TestResources testResources;

    @Inject
    public TestResultGroupNode(TestResources testResources,
                               @Assisted TestResult result) {
        failureCount = result.getFailureCount();
        this.testResources = testResources;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return Promises.resolve(children);
    }

    @Override
    public String getName() {
        if (failureCount > 0) {
            return "There are " + failureCount + " test failures.";
        } else {
            return "Test passed.";
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        if (failureCount > 0) {
            presentation.setPresentableIcon(testResources.testResultsFail());
        } else {
            presentation.setPresentableIcon(testResources.testResultsPass());
        }
        presentation.setPresentableText(getName());
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
