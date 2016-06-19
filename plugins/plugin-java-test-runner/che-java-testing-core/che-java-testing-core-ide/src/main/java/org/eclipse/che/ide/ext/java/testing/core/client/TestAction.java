package org.eclipse.che.ide.ext.java.testing.core.client;

import org.eclipse.che.ide.api.action.DefaultActionGroup;

public interface TestAction {

    void addMainMenuItems(DefaultActionGroup testMainMenu);

    void addContextMenuItems(DefaultActionGroup testContextMenu);

}
