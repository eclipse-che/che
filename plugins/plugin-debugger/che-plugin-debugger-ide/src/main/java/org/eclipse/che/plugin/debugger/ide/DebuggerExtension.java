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
package org.eclipse.che.plugin.debugger.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.plugin.debugger.ide.actions.ChangeVariableValueAction;
import org.eclipse.che.plugin.debugger.ide.actions.DebugAction;
import org.eclipse.che.plugin.debugger.ide.actions.DeleteAllBreakpointsAction;
import org.eclipse.che.plugin.debugger.ide.actions.DisconnectDebuggerAction;
import org.eclipse.che.plugin.debugger.ide.actions.EditConfigurationsAction;
import org.eclipse.che.plugin.debugger.ide.actions.EvaluateExpressionAction;
import org.eclipse.che.plugin.debugger.ide.actions.ResumeExecutionAction;
import org.eclipse.che.plugin.debugger.ide.actions.ShowHideDebuggerPanelAction;
import org.eclipse.che.plugin.debugger.ide.actions.StepIntoAction;
import org.eclipse.che.plugin.debugger.ide.actions.StepOutAction;
import org.eclipse.che.plugin.debugger.ide.actions.StepOverAction;
import org.eclipse.che.plugin.debugger.ide.actions.SuspendAction;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationsGroup;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.ide.util.input.KeyCodeMap;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_DEBUG_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;
import static org.eclipse.che.ide.api.constraints.Constraints.LAST;

/**
 * Extension allows debug applications.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Anatoliy Bazko
 * @author Mykola Morhun
 */
@Singleton
@Extension(title = "Debugger", version = "4.1.0")
public class DebuggerExtension {

    private static final String EDIT_DEBUG_CONF_ID          = "editDebugConfigurations";
    private static final String DEBUG_ID                    = "debug";
    private static final String DISCONNECT_DEBUG_ID         = "disconnectDebug";
    private static final String STEP_INTO_ID                = "stepInto";
    private static final String STEP_OVER_ID                = "stepOver";
    private static final String STEP_OUT_ID                 = "stepOut";
    private static final String RESUME_EXECUTION_ID         = "resumeExecution";
    private static final String SUSPEND_EXECUTION_ID        = "suspendExecution";
    private static final String EVALUATE_EXPRESSION_ID      = "evaluateExpression";
    private static final String CHANGE_VARIABLE_VALUE_ID    = "changeVariableValue";
    private static final String SHOW_HIDE_DEBUGGER_PANEL_ID = "showHideDebuggerPanel";

    @Inject
    public DebuggerExtension(DebuggerResources debuggerResources,
                             DebuggerLocalizationConstant localizationConstants,
                             ActionManager actionManager,
                             DebugAction debugAction,
                             DisconnectDebuggerAction disconnectDebuggerAction,
                             StepIntoAction stepIntoAction,
                             StepOverAction stepOverAction,
                             StepOutAction stepOutAction,
                             ResumeExecutionAction resumeExecutionAction,
                             SuspendAction suspendAction,
                             EvaluateExpressionAction evaluateExpressionAction,
                             DeleteAllBreakpointsAction deleteAllBreakpointsAction,
                             ChangeVariableValueAction changeVariableValueAction,
                             ShowHideDebuggerPanelAction showHideDebuggerPanelAction,
                             EditConfigurationsAction editConfigurationsAction,
                             DebugConfigurationsGroup configurationsGroup,
                             DebuggerPresenter debuggerPresenter,
                             KeyBindingAgent keyBinding) {
        debuggerResources.getCss().ensureInjected();

        final DefaultActionGroup runMenu = (DefaultActionGroup)actionManager.getAction(GROUP_RUN);

        // register actions
        actionManager.registerAction(EDIT_DEBUG_CONF_ID, editConfigurationsAction);
        actionManager.registerAction(DEBUG_ID, debugAction);
        actionManager.registerAction(DISCONNECT_DEBUG_ID, disconnectDebuggerAction);
        actionManager.registerAction(STEP_INTO_ID, stepIntoAction);
        actionManager.registerAction(STEP_OVER_ID, stepOverAction);
        actionManager.registerAction(STEP_OUT_ID, stepOutAction);
        actionManager.registerAction(RESUME_EXECUTION_ID, resumeExecutionAction);
        actionManager.registerAction(SUSPEND_EXECUTION_ID, suspendAction);
        actionManager.registerAction(EVALUATE_EXPRESSION_ID, evaluateExpressionAction);
        actionManager.registerAction(CHANGE_VARIABLE_VALUE_ID, changeVariableValueAction);
        actionManager.registerAction(SHOW_HIDE_DEBUGGER_PANEL_ID, showHideDebuggerPanelAction);

        // create group for selecting (changing) debug configurations
        final DefaultActionGroup debugActionGroup = new DefaultActionGroup(localizationConstants.debugActionTitle(),
                                                                           true,
                                                                           actionManager);
        debugActionGroup.add(debugAction);
        debugActionGroup.addSeparator();
        debugActionGroup.add(configurationsGroup);

        // add actions in main menu
        runMenu.addSeparator();
        runMenu.add(debugActionGroup, LAST);
        runMenu.add(editConfigurationsAction, LAST);
        runMenu.add(disconnectDebuggerAction, LAST);
        runMenu.addSeparator();
        runMenu.add(stepIntoAction, LAST);
        runMenu.add(stepOverAction, LAST);
        runMenu.add(stepOutAction, LAST);
        runMenu.add(resumeExecutionAction, LAST);
        runMenu.add(suspendAction, new Constraints(Anchor.BEFORE, RESUME_EXECUTION_ID));
        runMenu.addSeparator();
        runMenu.add(evaluateExpressionAction, LAST);

        // create debugger toolbar action group
        DefaultActionGroup debuggerToolbarActionGroup = new DefaultActionGroup(actionManager);
        debuggerToolbarActionGroup.add(resumeExecutionAction);
        debuggerToolbarActionGroup.add(suspendAction);
        debuggerToolbarActionGroup.add(stepIntoAction);
        debuggerToolbarActionGroup.add(stepOverAction);
        debuggerToolbarActionGroup.add(stepOutAction);
        debuggerToolbarActionGroup.add(disconnectDebuggerAction);
        debuggerToolbarActionGroup.add(deleteAllBreakpointsAction);
        debuggerToolbarActionGroup.add(changeVariableValueAction);
        debuggerToolbarActionGroup.add(evaluateExpressionAction);
        debuggerPresenter.getDebuggerToolbar().bindMainGroup(debuggerToolbarActionGroup);

        // add actions in 'Debug' context menu
        final DefaultActionGroup debugContextMenuGroup = (DefaultActionGroup)actionManager.getAction(GROUP_DEBUG_CONTEXT_MENU);
        debugContextMenuGroup.add(debugAction);
        debugContextMenuGroup.addSeparator();

        // keys binding
        keyBinding.getGlobal().addKey(new KeyBuilder().alt().shift().charCode(KeyCodeMap.F9).build(), EDIT_DEBUG_CONF_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().shift().charCode(KeyCodeMap.F9).build(), DEBUG_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode(KeyCodeMap.F2).build(), DISCONNECT_DEBUG_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F7).build(), STEP_INTO_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F8).build(), STEP_OVER_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().shift().charCode(KeyCodeMap.F8).build(), STEP_OUT_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F9).build(), RESUME_EXECUTION_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F8).build(), EVALUATE_EXPRESSION_ID);
        keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F2).build(), CHANGE_VARIABLE_VALUE_ID);

        if (UserAgent.isMac()) {
            keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode('5').build(), SHOW_HIDE_DEBUGGER_PANEL_ID);
        } else {
            keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode('5').build(), SHOW_HIDE_DEBUGGER_PANEL_ID);
        }
    }
}
