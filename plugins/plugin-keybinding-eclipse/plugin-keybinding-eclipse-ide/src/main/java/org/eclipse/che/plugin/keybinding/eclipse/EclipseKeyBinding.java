/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.keybinding.eclipse;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.api.keybinding.Scheme;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.input.KeyCodeMap;

import javax.inject.Inject;

import static org.eclipse.che.ide.core.StandardComponentInitializer.CLOSE_ACTIVE_EDITOR;
import static org.eclipse.che.ide.core.StandardComponentInitializer.FORMAT;
import static org.eclipse.che.ide.core.StandardComponentInitializer.FULL_TEXT_SEARCH;
import static org.eclipse.che.ide.core.StandardComponentInitializer.NAVIGATE_TO_FILE;
import static org.eclipse.che.ide.core.StandardComponentInitializer.RENAME;
import static org.eclipse.che.ide.core.StandardComponentInitializer.SHOW_REFERENCE;
import static org.eclipse.che.ide.core.StandardComponentInitializer.SIGNATURE_HELP;
import static org.eclipse.che.ide.ext.git.client.GitExtension.GIT_COMPARE_WITH_LATEST;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.JAVA_CLASS_STRUCTURE;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.JAVA_CUT_REFACTORING;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.JAVA_FIND_USAGES;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.JAVA_MOVE_REFACTORING;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.JAVA_RENAME_REFACTORING;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.OPEN_JAVA_DECLARATION;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.ORGANIZE_IMPORTS;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.PARAMETERS_INFO;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.QUICK_FIX;
import static org.eclipse.che.ide.ext.java.client.JavaExtension.SHOW_QUICK_DOC;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.DEBUG_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.DISCONNECT_DEBUG_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.EDIT_DEBUG_CONF_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.EVALUATE_EXPRESSION_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.RESUME_EXECUTION_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.STEP_INTO_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.STEP_OUT_ID;
import static org.eclipse.che.plugin.debugger.ide.DebuggerExtension.STEP_OVER_ID;
import static org.eclipse.che.plugin.testing.junit.ide.JUnitTestAction.TEST_ACTION_RUN_ALL;
import static org.eclipse.che.plugin.testing.junit.ide.JUnitTestAction.TEST_ACTION_RUN_CLASS;

@Extension(title = "Key Binding Eclipse")
public class EclipseKeyBinding {

    private final Scheme scheme;

    @Inject
    public EclipseKeyBinding(KeyBindingAgent agent) {
        scheme = agent.getEclipse();
        registerStandardComponentKeys();
        registerDebuggerKeys();
        registerJavaKeys();
        registerJUnitKeys();
        registerGitKeys();
    }

    protected void registerStandardComponentKeys() {
        scheme.addKey(new KeyBuilder().action().charCode('R').build(), NAVIGATE_TO_FILE);
        scheme.addKey(new KeyBuilder().control().charCode('h').build(), FULL_TEXT_SEARCH);
        scheme.addKey(new KeyBuilder().action().charCode('G').build(), SHOW_REFERENCE);
        scheme.addKey(new KeyBuilder().action().charCode('F').build(), FORMAT);
        scheme.addKey(new KeyBuilder().charCode(KeyCodeMap.F2).build(), RENAME);
        scheme.addKey(new KeyBuilder().control().charCode('p').build(), SIGNATURE_HELP);

        if (UserAgent.isMac()) {
            scheme.addKey(new KeyBuilder().action().charCode('w').build(), CLOSE_ACTIVE_EDITOR);
        } else {
            scheme.addKey(new KeyBuilder().alt().charCode('w').build(), CLOSE_ACTIVE_EDITOR); // XXX
        }
    }

    protected void registerDebuggerKeys() {
        // DebuggerExtension
        scheme.addKey(new KeyBuilder().alt().shift().charCode(KeyCodeMap.F9).build(), EDIT_DEBUG_CONF_ID);
        scheme.addKey(new KeyBuilder().action().charCode(KeyCodeMap.F11).build(), DEBUG_ID);
        scheme.addKey(new KeyBuilder().action().charCode(KeyCodeMap.F2).build(), DISCONNECT_DEBUG_ID);
        scheme.addKey(new KeyBuilder().charCode(KeyCodeMap.F5).build(), STEP_INTO_ID);
        scheme.addKey(new KeyBuilder().charCode(KeyCodeMap.F6).build(), STEP_OVER_ID);
        scheme.addKey(new KeyBuilder().charCode(KeyCodeMap.F7).build(), STEP_OUT_ID);
        scheme.addKey(new KeyBuilder().charCode(KeyCodeMap.F8).build(), RESUME_EXECUTION_ID);
        scheme.addKey(new KeyBuilder().action().charCode('D').build(), EVALUATE_EXPRESSION_ID);
    }

    protected void registerJavaKeys() {
        // Java
        scheme.addKey(new KeyBuilder().shift().charCode(KeyCodeMap.F2).build(), SHOW_QUICK_DOC);
        scheme.addKey(new KeyBuilder().action().charCode('1').build(), QUICK_FIX);
        scheme.addKey(new KeyBuilder().control().charCode('p').build(), PARAMETERS_INFO);
        scheme.addKey(new KeyBuilder().action().charCode('o').build(), JAVA_CLASS_STRUCTURE);
        scheme.addKey(new KeyBuilder().action().charCode('O').build(), ORGANIZE_IMPORTS);
        scheme.addKey(new KeyBuilder().charCode(KeyCodeMap.F3).build(), OPEN_JAVA_DECLARATION);
        if (UserAgent.isMac()) {
            scheme.addKey(new KeyBuilder().alt().action().charCode('r').build(), JAVA_RENAME_REFACTORING);
            scheme.addKey(new KeyBuilder().alt().action().charCode('v').build(), JAVA_MOVE_REFACTORING);
        } else {
            scheme.addKey(new KeyBuilder().alt().charCode('R').build(), JAVA_RENAME_REFACTORING);
            scheme.addKey(new KeyBuilder().alt().charCode('V').build(), JAVA_MOVE_REFACTORING);
        }
        scheme.addKey(new KeyBuilder().action().charCode('x').build(), JAVA_CUT_REFACTORING);
        scheme.addKey(new KeyBuilder().control().alt().charCode('h').build(), JAVA_FIND_USAGES);
    }

    private void registerJUnitKeys() {
        if (UserAgent.isMac()) {
            scheme.addKey(new KeyBuilder().control().alt().charCode('z').build(), TEST_ACTION_RUN_ALL);
            scheme.addKey(new KeyBuilder().control().shift().charCode('z').build(), TEST_ACTION_RUN_CLASS);
        } else {
            scheme.addKey(new KeyBuilder().action().alt().charCode('z').build(), TEST_ACTION_RUN_ALL);
            scheme.addKey(new KeyBuilder().action().shift().charCode('z').build(), TEST_ACTION_RUN_CLASS);
        }
    }

    protected void registerGitKeys() {
        // Git
        scheme.addKey(new KeyBuilder().action().alt().charCode('d').build(), GIT_COMPARE_WITH_LATEST);
    }
}
