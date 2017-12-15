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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Set;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
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
public class TestingExtension extends TestingExtensionTemplate {
  public static final String RUN_TEST = "RunTest";
  public static final String DEBUG_TEST = "DebugTest";

  @Inject
  public TestingExtension(
      ActionManager actionManager,
      TestLocalizationConstant localization,
      Set<TestAction> testActions,
      KeyBindingAgent keyBinding,
      DebugTestAction debugTestAction,
      RunTestAction runTestAction,
      PreferencesManager preferencesManager) {
    super(
        actionManager,
        localization,
        testActions,
        keyBinding,
        debugTestAction,
        runTestAction,
        "",
        preferencesManager);
  }

  @Override
  protected void registerKeyBinding(KeyBindingAgent keyBinding) {
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
  }

  protected DefaultActionGroup createTestMainMenuActionGroup(
      String groupName, ActionManager actionManager, PreferencesManager preferencesManager) {
    return new DefaultActionGroup(groupName, true, actionManager);
  }
}
