package org.eclipse.che.ide.ext.java.testing.junit4x.client.view.navigation.nodes;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.testing.junit4x.client.TestResources;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;
import java.util.List;

public class TestResultClassNode extends AbstractTreeNode implements HasPresentation {

    private String className;
    private final JavaResources javaResources;
    private NodePresentation nodePresentation;

    @Inject
    public TestResultClassNode(JavaResources javaResources,
                               @Assisted String className) {
        this.className = className;
        this.javaResources = javaResources;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return Promises.resolve(children);
    }

    @Override
    public String getName() {
        return "Class: " + className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setInfoText("(" + className + ")");
        presentation.setPresentableText(className.substring(className.lastIndexOf(".") + 1));
        presentation.setPresentableIcon(javaResources.svgClassItem());
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
