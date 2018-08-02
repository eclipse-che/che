/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.ide;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Set;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.plugin.testing.ide.action.DebugTestAction;
import org.eclipse.che.plugin.testing.ide.action.RunTestAction;
import org.eclipse.che.plugin.testing.ide.action.TestAction;

/**
 * Java test extension.
 *
 * @author Mirage Abeysekara
 */
@Singleton
@Extension(title = "Testing Extension", version = "1.0.0")
public class TestingExtension {
  public static final String RUN_TEST = "RunTest";
  public static final String DEBUG_TEST = "DebugTest";

  @Inject
  public TestingExtension(
      ActionManager actionManager,
      TestLocalizationConstant localization,
      Set<TestAction> testActions,
      KeyBindingAgent keyBinding,
      DebugTestAction debugTestAction,
      RunTestAction runTestAction) {

    DefaultActionGroup runMenu = (DefaultActionGroup) actionManager.getAction(GROUP_RUN);
    DefaultActionGroup testMainMenu =
        new DefaultActionGroup(localization.actionGroupMenuName(), true, actionManager);
    actionManager.registerAction("TestingMainGroup", testMainMenu);

    for (TestAction testAction : testActions) {
      testAction.addMainMenuItems(testMainMenu);
      testMainMenu.addSeparator();
    }
    actionManager.registerAction(RUN_TEST, runTestAction);
    actionManager.registerAction(DEBUG_TEST, debugTestAction);

    if (UserAgent.isMac()) {
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().control().alt().charCode('x').build(), DEBUG_TEST);
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().control().alt().charCode('z').build(), RUN_TEST);
    } else {
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().action().alt().charCode('x').build(), DEBUG_TEST);
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().action().alt().charCode('z').build(), RUN_TEST);
    }

    testMainMenu.add(runTestAction);
    testMainMenu.add(debugTestAction);
    runMenu.addSeparator();
    runMenu.add(testMainMenu);
    DefaultActionGroup explorerMenu =
        (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
    DefaultActionGroup testContextMenu =
        new DefaultActionGroup(localization.contextActionGroupMenuName(), true, actionManager);
    actionManager.registerAction("TestingContextGroup", testContextMenu);
    for (TestAction testAction : testActions) {
      testAction.addContextMenuItems(testContextMenu);
      testContextMenu.addSeparator();
    }

    explorerMenu.addSeparator();
    explorerMenu.add(testContextMenu);
    explorerMenu.addSeparator();

    DefaultActionGroup editorContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction(IdeActions.GROUP_EDITOR_CONTEXT_MENU);
    editorContextMenuGroup.addSeparator();
    editorContextMenuGroup.add(testMainMenu);
  }
}
