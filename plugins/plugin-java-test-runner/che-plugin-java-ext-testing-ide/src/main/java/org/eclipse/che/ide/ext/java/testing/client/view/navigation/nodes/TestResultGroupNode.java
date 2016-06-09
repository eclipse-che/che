package org.eclipse.che.ide.ext.java.testing.client.view.navigation.nodes;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;

import java.util.List;

public class TestResultGroupNode extends AbstractTreeNode {

    int failureCount;

    public TestResultGroupNode(TestResult result) {
        failureCount = result.getFailureCount();
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

}
