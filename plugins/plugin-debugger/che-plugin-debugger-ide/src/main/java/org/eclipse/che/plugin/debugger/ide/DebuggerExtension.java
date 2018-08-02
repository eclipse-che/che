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
package org.eclipse.che.plugin.debugger.ide;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_DEBUG_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;
import static org.eclipse.che.ide.api.action.IdeActions.TOOL_WINDOWS_GROUP;
import static org.eclipse.che.ide.api.constraints.Anchor.AFTER;
import static org.eclipse.che.ide.api.constraints.Constraints.LAST;
import static org.eclipse.che.ide.core.StandardComponentInitializer.COMMAND_EXPLORER_DISPLAYING_MODE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.debug.BreakpointResources;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.input.KeyCodeMap;
import org.eclipse.che.plugin.debugger.ide.actions.AddWatchExpressionAction;
import org.eclipse.che.plugin.debugger.ide.actions.DebugAction;
import org.eclipse.che.plugin.debugger.ide.actions.DebuggerDisplayingModeAction;
import org.eclipse.che.plugin.debugger.ide.actions.DeleteAllBreakpointsAction;
import org.eclipse.che.plugin.debugger.ide.actions.DeleteBreakpointAction;
import org.eclipse.che.plugin.debugger.ide.actions.DisableBreakpointAction;
import org.eclipse.che.plugin.debugger.ide.actions.DisconnectDebuggerAction;
import org.eclipse.che.plugin.debugger.ide.actions.EditConfigurationsAction;
import org.eclipse.che.plugin.debugger.ide.actions.EditDebugVariableAction;
import org.eclipse.che.plugin.debugger.ide.actions.EnableBreakpointAction;
import org.eclipse.che.plugin.debugger.ide.actions.EvaluateExpressionAction;
import org.eclipse.che.plugin.debugger.ide.actions.RemoveWatchExpressionAction;
import org.eclipse.che.plugin.debugger.ide.actions.ResumeExecutionAction;
import org.eclipse.che.plugin.debugger.ide.actions.RunToCursorAction;
import org.eclipse.che.plugin.debugger.ide.actions.ShowHideDebuggerPanelAction;
import org.eclipse.che.plugin.debugger.ide.actions.StepIntoAction;
import org.eclipse.che.plugin.debugger.ide.actions.StepOutAction;
import org.eclipse.che.plugin.debugger.ide.actions.StepOverAction;
import org.eclipse.che.plugin.debugger.ide.actions.SuspendAction;
import org.eclipse.che.plugin.debugger.ide.configuration.DebugConfigurationsGroup;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.breakpoint.BreakpointActionGroup;
import org.eclipse.che.plugin.debugger.ide.debug.breakpoint.BreakpointConfigurationAction;

/**
 * Extension allows debug applications.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Anatoliy Bazko
 * @author Mykola Morhun
 * @author Oleksandr Andriienko
 */
@Singleton
@Extension(title = "Debugger", version = "4.1.0")
public class DebuggerExtension {

  public static final String EDIT_DEBUG_CONF_ID = "editDebugConfigurations";
  public static final String DEBUG_ID = "debug";
  public static final String DISCONNECT_DEBUG_ID = "disconnectDebug";
  public static final String STEP_INTO_ID = "stepInto";
  public static final String STEP_OVER_ID = "stepOver";
  public static final String STEP_OUT_ID = "stepOut";
  public static final String RESUME_EXECUTION_ID = "resumeExecution";
  public static final String RUN_TO_CURSOR_ID = "runToCursor";
  public static final String SUSPEND_EXECUTION_ID = "suspendExecution";
  public static final String EVALUATE_EXPRESSION_ID = "evaluateExpression";
  public static final String EDIT_DEBUG_VARIABLE_ID = "editDebugVariable";
  public static final String ADD_WATCH_EXPRESSION = "addWatchExpression";
  public static final String REMOVE_WATCH_EXPRESSION = "removeWatchExpression";
  public static final String SHOW_HIDE_DEBUGGER_PANEL_ID = "showHideDebuggerPanel";
  public static final String BREAKPOINT_CONFIGURATION_ID = "breakpointSettings";
  public static final String BREAKPOINT_CONTEXT_MENU = "breakpointContextMenu";
  public static final String DISABLE_BREAKPOINT_ID = "disableBreakpoint";
  public static final String ENABLE_BREAKPOINT_ID = "enableBreakpoint";
  public static final String DELETE_BREAKPOINT_ID = "deleteBreakpoint";
  public static final String DEBUGGER_DISPLAYING_MODE_ID = "debuggerDisplayingMode";

  public static final String BREAKPOINT = "breakpoint";

  @Inject
  public DebuggerExtension(
      DebuggerResources debuggerResources,
      BreakpointResources breakpointResources,
      DebuggerLocalizationConstant localizationConstants,
      ActionManager actionManager,
      DebugAction debugAction,
      DisconnectDebuggerAction disconnectDebuggerAction,
      StepIntoAction stepIntoAction,
      StepOverAction stepOverAction,
      StepOutAction stepOutAction,
      RunToCursorAction runToCursorAction,
      ResumeExecutionAction resumeExecutionAction,
      SuspendAction suspendAction,
      EvaluateExpressionAction evaluateExpressionAction,
      DeleteAllBreakpointsAction deleteAllBreakpointsAction,
      EditDebugVariableAction editDebugVariableAction,
      ShowHideDebuggerPanelAction showHideDebuggerPanelAction,
      EditConfigurationsAction editConfigurationsAction,
      BreakpointConfigurationAction breakpointConfigurationAction,
      AddWatchExpressionAction addWatchExpressionAction,
      RemoveWatchExpressionAction removeWatchExpressionAction,
      DebugConfigurationsGroup configurationsGroup,
      DebuggerPresenter debuggerPresenter,
      KeyBindingAgent keyBinding,
      BreakpointActionGroup breakpointActionGroup,
      EnableBreakpointAction enableBreakpointAction,
      DisableBreakpointAction disableBreakpointAction,
      DeleteBreakpointAction deleteBreakpointAction,
      DebuggerDisplayingModeAction debuggerDisplayingModeAction) {
    debuggerResources.getCss().ensureInjected();
    breakpointResources.getCss().ensureInjected();

    final DefaultActionGroup runMenu = (DefaultActionGroup) actionManager.getAction(GROUP_RUN);

    // register actions
    actionManager.registerAction(EDIT_DEBUG_CONF_ID, editConfigurationsAction);
    actionManager.registerAction(DEBUG_ID, debugAction);
    actionManager.registerAction(DISCONNECT_DEBUG_ID, disconnectDebuggerAction);
    actionManager.registerAction(STEP_INTO_ID, stepIntoAction);
    actionManager.registerAction(STEP_OVER_ID, stepOverAction);
    actionManager.registerAction(STEP_OUT_ID, stepOutAction);
    actionManager.registerAction(RUN_TO_CURSOR_ID, runToCursorAction);
    actionManager.registerAction(RESUME_EXECUTION_ID, resumeExecutionAction);
    actionManager.registerAction(SUSPEND_EXECUTION_ID, suspendAction);
    actionManager.registerAction(EVALUATE_EXPRESSION_ID, evaluateExpressionAction);
    actionManager.registerAction(EDIT_DEBUG_VARIABLE_ID, editDebugVariableAction);
    actionManager.registerAction(ADD_WATCH_EXPRESSION, addWatchExpressionAction);
    actionManager.registerAction(REMOVE_WATCH_EXPRESSION, removeWatchExpressionAction);
    actionManager.registerAction(SHOW_HIDE_DEBUGGER_PANEL_ID, showHideDebuggerPanelAction);
    actionManager.registerAction(BREAKPOINT_CONFIGURATION_ID, breakpointConfigurationAction);
    actionManager.registerAction(ENABLE_BREAKPOINT_ID, enableBreakpointAction);
    actionManager.registerAction(DISABLE_BREAKPOINT_ID, disableBreakpointAction);
    actionManager.registerAction(DELETE_BREAKPOINT_ID, deleteBreakpointAction);
    actionManager.registerAction(DEBUGGER_DISPLAYING_MODE_ID, debuggerDisplayingModeAction);

    // create group for selecting (changing) debug configurations
    final DefaultActionGroup debugActionGroup =
        new DefaultActionGroup(localizationConstants.debugActionTitle(), true, actionManager);
    debugActionGroup.add(debugAction);
    debugActionGroup.addSeparator();
    debugActionGroup.add(configurationsGroup);

    // breakpoint context menu
    breakpointActionGroup.add(enableBreakpointAction);
    breakpointActionGroup.add(disableBreakpointAction);
    breakpointActionGroup.add(breakpointConfigurationAction);
    breakpointActionGroup.add(deleteBreakpointAction);
    actionManager.registerAction(BREAKPOINT_CONTEXT_MENU, breakpointActionGroup);

    // add actions in main menu
    runMenu.addSeparator();
    runMenu.add(debugActionGroup, LAST);
    runMenu.add(editConfigurationsAction, LAST);
    runMenu.add(disconnectDebuggerAction, LAST);
    runMenu.addSeparator();
    runMenu.add(stepIntoAction, LAST);
    runMenu.add(stepOverAction, LAST);
    runMenu.add(stepOutAction, LAST);
    runMenu.add(runToCursorAction, LAST);
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
    debuggerToolbarActionGroup.add(runToCursorAction);
    debuggerToolbarActionGroup.add(disconnectDebuggerAction);
    debuggerToolbarActionGroup.add(deleteAllBreakpointsAction);
    debuggerToolbarActionGroup.add(evaluateExpressionAction);
    debuggerPresenter.getDebuggerToolbar().bindMainGroup(debuggerToolbarActionGroup);

    DefaultActionGroup watchDebuggerActionGroup = new DefaultActionGroup(actionManager);
    watchDebuggerActionGroup.add(addWatchExpressionAction);
    watchDebuggerActionGroup.add(removeWatchExpressionAction);

    watchDebuggerActionGroup.add(editDebugVariableAction);

    // create watch debugger toolbar action group
    debuggerPresenter.getWatchExpressionToolbar().bindMainGroup(watchDebuggerActionGroup);

    // add actions in 'Debug' context menu
    final DefaultActionGroup debugContextMenuGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_DEBUG_CONTEXT_MENU);
    debugContextMenuGroup.add(debugAction);
    debugContextMenuGroup.addSeparator();

    DefaultActionGroup toolWindowGroup =
        (DefaultActionGroup) actionManager.getAction(TOOL_WINDOWS_GROUP);
    toolWindowGroup.add(
        debuggerDisplayingModeAction, new Constraints(AFTER, COMMAND_EXPLORER_DISPLAYING_MODE));

    // keys binding
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().alt().shift().charCode(KeyCodeMap.F9).build(), EDIT_DEBUG_CONF_ID);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().shift().charCode(KeyCodeMap.F9).build(), DEBUG_ID);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().action().charCode(KeyCodeMap.F2).build(), DISCONNECT_DEBUG_ID);
    keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F7).build(), STEP_INTO_ID);
    keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.F8).build(), STEP_OVER_ID);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().shift().charCode(KeyCodeMap.F8).build(), STEP_OUT_ID);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().charCode(KeyCodeMap.F9).build(), RESUME_EXECUTION_ID);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F9).build(), RUN_TO_CURSOR_ID);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F8).build(), EVALUATE_EXPRESSION_ID);
    keyBinding
        .getGlobal()
        .addKey(new KeyBuilder().charCode(KeyCodeMap.F2).build(), EDIT_DEBUG_VARIABLE_ID);

    if (UserAgent.isMac()) {
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().action().charCode('5').build(), SHOW_HIDE_DEBUGGER_PANEL_ID);
      keyBinding
          .getGlobal()
          .addKey(
              new KeyBuilder().action().control().charCode('5').build(),
              DEBUGGER_DISPLAYING_MODE_ID);
    } else {
      keyBinding
          .getGlobal()
          .addKey(new KeyBuilder().alt().charCode('5').build(), SHOW_HIDE_DEBUGGER_PANEL_ID);
      keyBinding
          .getGlobal()
          .addKey(
              new KeyBuilder().action().alt().charCode('5').build(), DEBUGGER_DISPLAYING_MODE_ID);
    }
  }
}
