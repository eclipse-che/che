/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.ide;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;

import java.util.Set;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.plugin.testing.ide.action.DebugTestAction;
import org.eclipse.che.plugin.testing.ide.action.RunTestAction;
import org.eclipse.che.plugin.testing.ide.action.TestAction;

/** Java test extension template. */
public abstract class TestingExtensionTemplate {
  public static final String RUN_TEST = "RunTest";
  public static final String DEBUG_TEST = "DebugTest";
  private DefaultActionGroup testMainMenu;
  private DefaultActionGroup testContextMenu;

  public TestingExtensionTemplate(
      ActionManager actionManager,
      TestLocalizationConstant localization,
      Set<? extends TestAction> testActions,
      KeyBindingAgent keyBinding,
      DebugTestAction debugTestAction,
      RunTestAction runTestAction,
      String mainGroupActionIdPrefix,
      PreferencesManager preferencesManager) {

    DefaultActionGroup runMenu = (DefaultActionGroup) actionManager.getAction(GROUP_RUN);
    testMainMenu =
        createTestMainMenuActionGroup(
            localization.actionGroupMenuName(), actionManager, preferencesManager);
    actionManager.registerAction(mainGroupActionIdPrefix + "TestingMainGroup", testMainMenu);

    for (TestAction testAction : testActions) {
      testAction.addMainMenuItems(testMainMenu);
      testMainMenu.addSeparator();
    }
    actionManager.registerAction(RUN_TEST, runTestAction);
    actionManager.registerAction(DEBUG_TEST, debugTestAction);

    registerKeyBinding(keyBinding);

    testMainMenu.add(runTestAction);
    testMainMenu.add(debugTestAction);

    runMenu.addSeparator();
    runMenu.add(testMainMenu);
    DefaultActionGroup explorerMenu =
        (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
    testContextMenu =
        createTestMainMenuActionGroup(
            localization.actionGroupMenuName(), actionManager, preferencesManager);
    actionManager.registerAction(mainGroupActionIdPrefix + "TestingContextGroup", testContextMenu);
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

  protected abstract void registerKeyBinding(KeyBindingAgent keyBinding);

  protected abstract DefaultActionGroup createTestMainMenuActionGroup(
      String localization, ActionManager actionManager, PreferencesManager preferencesManager);
}
