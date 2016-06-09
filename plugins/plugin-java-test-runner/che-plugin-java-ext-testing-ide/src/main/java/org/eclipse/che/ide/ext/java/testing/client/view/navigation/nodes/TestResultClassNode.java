package org.eclipse.che.ide.ext.java.testing.client.view.navigation.nodes;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;

import java.util.List;

public class TestResultClassNode extends AbstractTreeNode {

    private String className;
    public TestResultClassNode(String className) {
        this.className = className;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return Promises.resolve(children);
    }

    @Override
    public String getName() {
        return "Class: " +className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
