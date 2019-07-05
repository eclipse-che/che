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
package org.eclipse.che.plugin.keybinding;

import static org.eclipse.che.ide.api.action.IdeActions.CLOSE_ACTIVE_EDITOR;
import static org.eclipse.che.ide.api.action.IdeActions.FORMAT;
import static org.eclipse.che.ide.api.action.IdeActions.FULL_TEXT_SEARCH;
import static org.eclipse.che.ide.api.action.IdeActions.NAVIGATE_TO_FILE;
import static org.eclipse.che.ide.api.action.IdeActions.RENAME;
import static org.eclipse.che.ide.api.action.IdeActions.SHOW_REFERENCE;
import static org.eclipse.che.ide.api.action.IdeActions.SIGNATURE_HELP;
import static org.eclipse.che.ide.ext.git.client.GitExtension.GIT_COMPARE_WITH_LATEST;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.JAVA_CLASS_STRUCTURE;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.JAVA_CUT_REFACTORING;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.JAVA_FIND_USAGES;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.JAVA_MOVE_REFACTORING;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.JAVA_RENAME_REFACTORING;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.ORGANIZE_IMPORTS;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.QUICK_FIX;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.SHOW_QUICK_DOC;
import static org.eclipse.che.ide.keybinding.KeyBindingManager.SCHEME_ECLIPSE_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.DEBUG_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.DISCONNECT_DEBUG_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.EDIT_DEBUG_CONF_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.EVALUATE_EXPRESSION_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.RESUME_EXECUTION_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.STEP_INTO_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.STEP_OUT_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.STEP_OVER_ID;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;
import org.eclipse.che.ide.util.input.KeyCodeMap;

@Extension(title = "Key Binding Eclipse")
public class EclipseKeyBinding {

  @Inject
  public EclipseKeyBinding(KeyBindingAgent agent) {
    Map<String, CharCodeWithModifiers> keys = new HashMap<>();

    // Standard Component Keys
    keys.put(NAVIGATE_TO_FILE, new KeyBuilder().action().charCode('R').build());
    keys.put(FULL_TEXT_SEARCH, new KeyBuilder().control().charCode('h').build());
    keys.put(SHOW_REFERENCE, new KeyBuilder().action().charCode('G').build());
    keys.put(FORMAT, new KeyBuilder().action().charCode('F').build());
    keys.put(RENAME, new KeyBuilder().charCode(KeyCodeMap.F2).build());
    keys.put(SIGNATURE_HELP, new KeyBuilder().control().charCode('p').build());

    if (UserAgent.isMac()) {
      keys.put(CLOSE_ACTIVE_EDITOR, new KeyBuilder().action().charCode('w').build());
    } else {
      keys.put(CLOSE_ACTIVE_EDITOR, new KeyBuilder().alt().charCode('w').build()); // XXX
    }

    // Debugger Extension Keys
    keys.put(EDIT_DEBUG_CONF_ID, new KeyBuilder().alt().shift().charCode(KeyCodeMap.F9).build());
    keys.put(DEBUG_ID, new KeyBuilder().action().charCode(KeyCodeMap.F11).build());
    keys.put(DISCONNECT_DEBUG_ID, new KeyBuilder().action().charCode(KeyCodeMap.F2).build());
    keys.put(STEP_INTO_ID, new KeyBuilder().charCode(KeyCodeMap.F5).build());
    keys.put(STEP_OVER_ID, new KeyBuilder().charCode(KeyCodeMap.F6).build());
    keys.put(STEP_OUT_ID, new KeyBuilder().charCode(KeyCodeMap.F7).build());
    keys.put(RESUME_EXECUTION_ID, new KeyBuilder().charCode(KeyCodeMap.F8).build());
    keys.put(EVALUATE_EXPRESSION_ID, new KeyBuilder().action().charCode('D').build());

    // Java Keys
    keys.put(SHOW_QUICK_DOC, new KeyBuilder().shift().charCode(KeyCodeMap.F2).build());
    keys.put(QUICK_FIX, new KeyBuilder().action().charCode('1').build());
    keys.put(JAVA_CLASS_STRUCTURE, new KeyBuilder().action().charCode('o').build());
    keys.put(ORGANIZE_IMPORTS, new KeyBuilder().action().charCode('O').build());
    keys.put(JAVA_CUT_REFACTORING, new KeyBuilder().action().charCode('x').build());
    keys.put(JAVA_FIND_USAGES, new KeyBuilder().control().alt().charCode('h').build());
    if (UserAgent.isMac()) {
      keys.put(JAVA_RENAME_REFACTORING, new KeyBuilder().alt().action().charCode('r').build());
      keys.put(JAVA_MOVE_REFACTORING, new KeyBuilder().alt().action().charCode('v').build());
    } else {
      keys.put(JAVA_RENAME_REFACTORING, new KeyBuilder().alt().charCode('R').build());
      keys.put(JAVA_MOVE_REFACTORING, new KeyBuilder().alt().charCode('V').build());
    }

    // Git keys
    keys.put(GIT_COMPARE_WITH_LATEST, new KeyBuilder().action().alt().charCode('d').build());

    // Register keys
    agent.getScheme(SCHEME_ECLIPSE_ID).addKeys(keys);
  }
}
