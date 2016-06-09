package org.eclipse.che.ide.ext.java.testing.client.view.navigation.nodes;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.tree.HasAction;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.ext.java.testing.client.view.navigation.TestClassNavigation;

import java.util.List;

public class TestResultMethodNode extends AbstractTreeNode implements HasAction {

    private String methodName;
    private String stackTrace;
    private String message;
    private int lineNumber;
    private TestClassNavigation navigationHandler;

    public TestResultMethodNode(String methodName, String stackTrace,String message, int lineNumber,
                                TestClassNavigation navigationHandler) {
        this.methodName = methodName;
        this.stackTrace = stackTrace;
        this.message = message;
        this.lineNumber = lineNumber;
        this.navigationHandler = navigationHandler;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return null;
    }

    @Override
    public String getName() {
        if(message!=null && !message.isEmpty()){
            return "Method: " + methodName + " (" + message + ")";
        }
        return "Method: " + methodName;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    @Override
    public void actionPerformed() {
        if(getParent() instanceof TestResultClassNode) {
            String packagePath = ((TestResultClassNode)getParent()).getClassName().replace(".", "/") + ".java";
            navigationHandler.gotoClass(packagePath,lineNumber);
        }
    }
}
