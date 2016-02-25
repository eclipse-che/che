/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.jdi.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.ext.java.jdi.client.actions.ChangeVariableValueAction;
import org.eclipse.che.ide.ext.java.jdi.client.actions.DeleteAllBreakpointsAction;
import org.eclipse.che.ide.ext.java.jdi.client.actions.DisconnectDebuggerAction;
import org.eclipse.che.ide.ext.java.jdi.client.actions.EvaluateExpressionAction;
import org.eclipse.che.ide.ext.java.jdi.client.actions.RemoteDebugAction;
import org.eclipse.che.ide.ext.java.jdi.client.actions.ResumeExecutionAction;
import org.eclipse.che.ide.ext.java.jdi.client.actions.ShowHideDebuggerPanelAction;
import org.eclipse.che.ide.ext.java.jdi.client.actions.StepIntoAction;
import org.eclipse.che.ide.ext.java.jdi.client.actions.StepOutAction;
import org.eclipse.che.ide.ext.java.jdi.client.actions.StepOverAction;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerPresenter;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.FqnResolverFactory;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.JavaClassFqnResolver;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.JavaFqnResolver;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
import org.eclipse.che.ide.util.input.KeyCodeMap;

import static org.eclipse.che.ide.MimeType.APPLICATION_JAVA_CLASS;
import static org.eclipse.che.ide.MimeType.TEXT_X_JAVA;
import static org.eclipse.che.ide.MimeType.TEXT_X_JAVA_SOURCE;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;
import static org.eclipse.che.ide.api.constraints.Constraints.LAST;

/**
 * Extension allows debug Java web applications.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Anatoliy Bazko
 * @author Morhun Mykola
 */
@Singleton
@Extension(title = "Java Debugger", version = "3.0.0")
public class JavaRuntimeExtension {
    /** Channel for the messages containing debugger events. */
    public static final String EVENTS_CHANNEL     = "debugger:events:";
    /** Channel for the messages containing message which informs about debugger is disconnected. */
    public static final String DISCONNECT_CHANNEL = "debugger:disconnected:";

    private static final String REMOTE_DEBUG_ID             = "remoteDebug";
    private static final String DISCONNECT_DEBUG_ID         = "disconnectDebug";
    private static final String STEP_INTO_ID                = "stepInto";
    private static final String STEP_OVER_ID                = "stepOver";
    private static final String STEP_OUT_ID                 = "stepOut";
    private static final String RESUME_EXECUTION_ID         = "resumeExecution";
    private static final String EVALUATE_EXPRESSION_ID      = "evaluateExpression";
    private static final String CHANGE_VARIABLE_VALUE_ID    = "changeVariableValue";
    private static final String SHOW_HIDE_DEBUGGER_PANEL_ID = "showHideDebuggerPanel";

    @Inject
    public JavaRuntimeExtension(ActionManager actionManager,
                                RemoteDebugAction remoteDebugAction,
                                DisconnectDebuggerAction disconnectDebuggerAction,
                                StepIntoAction stepIntoAction,
                                StepOverAction stepOverAction,
                                StepOutAction stepOutAction,
                                ResumeExecutionAction resumeExecutionAction,
                                EvaluateExpressionAction evaluateExpressionAction,
                                DeleteAllBreakpointsAction deleteAllBreakpointsAction,
                                ChangeVariableValueAction changeVariableValueAction,
                                ShowHideDebuggerPanelAction showHideDebuggerPanelAction,
                                DebuggerManager debuggerManager,
                                DebuggerPresenter debuggerPresenter,
                                FqnResolverFactory resolverFactory,
                                JavaFqnResolver javaFqnResolver,
                                JavaClassFqnResolver javaClassFqnResolver,
                                KeyBindingAgent keyBinding) {
        final DefaultActionGroup runMenu = (DefaultActionGroup)actionManager.getAction(GROUP_RUN);

        // register actions
        actionManager.registerAction(REMOTE_DEBUG_ID, remoteDebugAction);
        actionManager.registerAction(DISCONNECT_DEBUG_ID, disconnectDebuggerAction);
        actionManager.registerAction(STEP_INTO_ID, stepIntoAction);
        actionManager.registerAction(STEP_OVER_ID, stepOverAction);
        actionManager.registerAction(STEP_OUT_ID, stepOutAction);
        actionManager.registerAction(RESUME_EXECUTION_ID, resumeExecutionAction);
        actionManager.registerAction(EVALUATE_EXPRESSION_ID, evaluateExpressionAction);
        actionManager.registerAction(CHANGE_VARIABLE_VALUE_ID, changeVariableValueAction);
        actionManager.registerAction(SHOW_HIDE_DEBUGGER_PANEL_ID, showHideDebuggerPanelAction);

        // add actions in main menu
        runMenu.addSeparator();
        runMenu.add(remoteDebugAction, LAST);
        runMenu.add(disconnectDebuggerAction, LAST);
        runMenu.addSeparator();
        runMenu.add(stepIntoAction, LAST);
        runMenu.add(stepOverAction, LAST);
        runMenu.add(stepOutAction, LAST);
        runMenu.add(resumeExecutionAction, LAST);
        runMenu.addSeparator();
        runMenu.add(evaluateExpressionAction, LAST);

        // create debugger toolbar action group
        DefaultActionGroup debuggerToolbarActionGroup = new DefaultActionGroup(actionManager);
        debuggerToolbarActionGroup.add(resumeExecutionAction);
        debuggerToolbarActionGroup.add(stepIntoAction);
        debuggerToolbarActionGroup.add(stepOverAction);
        debuggerToolbarActionGroup.add(stepOutAction);
        debuggerToolbarActionGroup.add(disconnectDebuggerAction);
        debuggerToolbarActionGroup.add(deleteAllBreakpointsAction);
        debuggerToolbarActionGroup.add(changeVariableValueAction);
        debuggerToolbarActionGroup.add(evaluateExpressionAction);
        debuggerPresenter.getDebuggerToolbar().bindMainGroup(debuggerToolbarActionGroup);

        // add actions in context menu
        DefaultActionGroup runContextGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RUN_CONTEXT_MENU);
        runContextGroup.add(remoteDebugAction);

        debuggerManager.registeredDebugger(MavenAttributes.MAVEN_ID, debuggerPresenter);
        resolverFactory.addResolver(TEXT_X_JAVA, javaFqnResolver);
        resolverFactory.addResolver("application/java", javaFqnResolver);
        resolverFactory.addResolver(APPLICATION_JAVA_CLASS, javaClassFqnResolver);
        resolverFactory.addResolver(TEXT_X_JAVA_SOURCE, javaFqnResolver);

        // keys binding
        keyBinding.getGlobal().addKey(new KeyBuilder().alt().shift().charCode(KeyCodeMap.F9).build(), REMOTE_DEBUG_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode(KeyCodeMap.F2).build(), DISCONNECT_DEBUG_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F7).build(), STEP_INTO_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F8).build(), STEP_OVER_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().shift().charCode(KeyCodeMap.F8).build(), STEP_OUT_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F9).build(), RESUME_EXECUTION_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F8).build(), EVALUATE_EXPRESSION_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F2).build(), CHANGE_VARIABLE_VALUE_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode('5').build(), SHOW_HIDE_DEBUGGER_PANEL_ID);
    }
}
