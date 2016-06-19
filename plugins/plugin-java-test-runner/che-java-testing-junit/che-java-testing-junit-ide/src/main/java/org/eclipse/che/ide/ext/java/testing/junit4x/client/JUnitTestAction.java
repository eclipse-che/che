package org.eclipse.che.ide.ext.java.testing.junit4x.client;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.ext.java.testing.core.client.TestAction;

public class JUnitTestAction implements TestAction {

    private final Action runClassTestAction;
    private final Action runAllTestAction;
    private final Action runClassContextTestAction;

    @Inject
    public JUnitTestAction(ActionManager actionManager, RunClassTestAction runClassTestAction,
                           RunAllTestAction runAllTestAction, RunClassContextTestAction runClassContextTestAction,
                           KeyBindingAgent keyBinding) {

        actionManager.registerAction("TestActionRunClass", runClassTestAction);
        actionManager.registerAction("TestActionRunAll", runAllTestAction);
        actionManager.registerAction("TestActionRunClassContext", runClassContextTestAction);

        keyBinding.getGlobal().addKey(new KeyBuilder().action().alt().charCode('z').build(),
                "TestActionRunAll");

        keyBinding.getGlobal().addKey(new KeyBuilder().action().shift().charCode('z').build(),
                "TestActionRunClass");

        this.runAllTestAction = runAllTestAction;
        this.runClassContextTestAction = runClassContextTestAction;
        this.runClassTestAction = runClassTestAction;
    }


    @Override
    public void addMainMenuItems(DefaultActionGroup testMainMenu) {
        testMainMenu.add(runClassTestAction);
        testMainMenu.add(runAllTestAction);
    }

    @Override
    public void addContextMenuItems(DefaultActionGroup testContextMenu) {
        testContextMenu.add(runClassContextTestAction);
        testContextMenu.add(runAllTestAction);
    }
}
