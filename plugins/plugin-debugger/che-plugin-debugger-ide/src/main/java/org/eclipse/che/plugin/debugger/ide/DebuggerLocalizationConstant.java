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

/**
 * I18n constants for the Debugger extension.
 *
 * @author Artem Zatsarynnyi
 */
public interface DebuggerLocalizationConstant extends com.google.gwt.i18n.client.Messages {

  /* actions */
  @Key("editDebugConfigurationsActionTitle")
  String editDebugConfigurationsActionTitle();

  @Key("breakpoints")
  String breakpoints();

  @Key("debug")
  String debug();

  @Key("disconnectDebugger")
  String disconnectDebugger();

  @Key("host")
  String host();

  @Key("port")
  String port();

  @Key("deleteAllBreakpoints")
  String deleteAllBreakpoints();

  @Key("resumeExecution")
  String resumeExecution();

  @Key("variables")
  String variables();

  @Key("stepInto")
  String stepInto();

  @Key("stepOver")
  String stepOver();

  @Key("stepOut")
  String stepOut();

  @Key("runToCursor")
  String runToCursor();

  @Key("suspend")
  String suspend();

  @Key("editDebugVariable")
  String editDebugVariable();

  @Key("evaluateExpression")
  String evaluateExpression();

  @Key("showHideDebuggerPanel")
  String showHideDebuggerPanel();

  @Key("breakpointConfiguration")
  String breakpointConfiguration();

  @Key("enableBreakpoint")
  String enableBreakpoint();

  @Key("disableBreakpoint")
  String disableBreakpoint();

  @Key("deleteBreakpoint")
  String deleteBreakpoint();

  /* actions descriptions */
  @Key("editDebugConfigurationsActionDescription")
  String editDebugConfigurationsActionDescription();

  @Key("debugConfigurationActionDescription")
  String debugConfigurationActionDescription();

  @Key("resumeExecutionDescription")
  String resumeExecutionDescription();

  @Key("disconnectDebuggerDescription")
  String disconnectDebuggerDescription();

  @Key("stepIntoDescription")
  String stepIntoDescription();

  @Key("stepOverDescription")
  String stepOverDescription();

  @Key("stepOutDescription")
  String stepOutDescription();

  @Key("runToCursorDescription")
  String runToCursorDescription();

  @Key("suspendDescription")
  String suspendDescription();

  @Key("deleteAllBreakpointsDescription")
  String deleteAllBreakpointsDescription();

  @Key("editDebugVariableDescription")
  String editDebugVariableDescription();

  @Key("evaluateExpressionDescription")
  String evaluateExpressionDescription();

  @Key("showHideDebuggerPanelDescription")
  String showHideDebuggerPanelDescription();

  @Key("enableBreakpointDescription")
  String enableBreakpointDescription();

  @Key("disableBreakpointDescription")
  String disableBreakpointDescription();

  @Key("deleteBreakpointDescription")
  String deleteBreakpointDescription();

  /* messages */
  @Key("debugger.connecting.title")
  String debuggerConnectingTitle(String address);

  @Key("debugger.connected.title")
  String debuggerConnectedTitle();

  @Key("debugger.connected.description")
  String debuggerConnectedDescription(String address);

  @Key("debugger.disconnected.title")
  String debuggerDisconnectedTitle();

  @Key("debugger.disconnected.description")
  String debuggerDisconnectedDescription(String address);

  @Key("debugger.already.connected")
  String debuggerAlreadyConnected();

  @Key("failed.to.connect.to.remote.debugger.description")
  String failedToConnectToRemoteDebuggerDescription(String address, String cause);

  /* ChangeValueView */
  @Key("view.breakpoint.configuration.title")
  String breakpointConfigurationTitle();

  /* ChangeValueView */
  @Key("view.changeValue.title")
  String changeValueViewTitle();

  @Key("view.changeValue.expressionField.title")
  String changeValueViewExpressionFieldTitle(String varName);

  @Key("view.changeValue.changeButton.title")
  String changeValueViewChangeButtonTitle();

  @Key("view.changeValue.cancelButton.title")
  String changeValueViewCancelButtonTitle();

  /* EvaluateExpressionView */
  @Key("view.evaluateExpression.title")
  String evaluateExpressionViewTitle();

  @Key("view.evaluateExpression.expressionField.title")
  String evaluateExpressionViewExpressionFieldTitle();

  @Key("view.evaluateExpression.resultField.title")
  String evaluateExpressionViewResultFieldTitle();

  @Key("view.evaluateExpression.evaluateButton.title")
  String evaluateExpressionViewEvaluateButtonTitle();

  @Key("view.evaluateExpression.closeButton.title")
  String evaluateExpressionViewCloseButtonTitle();

  @Key("evaluateExpressionFailed")
  String evaluateExpressionFailed(String reason);

  @Key("connect.to.remote")
  String connectToRemote();

  @Key("debugActionTitle")
  String debugActionTitle();

  @Key("debugActionDescription")
  String debugActionDescription();

  /* EditConfigurations */
  @Key("view.editConfigurations.placeholder")
  String editConfigurationsViewPlaceholder();

  @Key("view.editConfigurations.title")
  String editConfigurationsViewTitle();

  @Key("view.editConfigurations.header")
  String editConfigurationsViewHeader();

  @Key("view.editConfigurations.hint")
  String editConfigurationsViewHint();

  @Key("view.editConfigurations.name.text")
  String editConfigurationsViewNameText();

  @Key("view.editConfigurations.remove.title")
  String editConfigurationsViewRemoveTitle();

  @Key("view.editConfigurations.remove.confirmation")
  String editConfigurationsRemoveConfirmation(String configurationName);

  @Key("view.editConfigurations.saveChanges.title")
  String editConfigurationsSaveChangesTitle();

  @Key("view.editConfigurations.saveChanges.text")
  String editConfigurationsSaveChangesConfirmation(String configurationName);

  @Key("view.editConfigurations.saveChanges.discard")
  String editConfigurationsSaveChangesDiscard();

  @Key("debugger.frames.title")
  String debuggerFramesTitle();

  @Key("debugger.threadNotSuspend")
  String debuggerThreadNotSuspend();

  /* Breakpoint Configuration */
  @Key("view.breakpointConfiguration.condition")
  String viewBreakpointConfigurationCondition();

  @Key("view.breakpointConfiguration.enabled")
  String viewBreakpointConfigurationEnabled();

  @Key("view.breakpointConfiguration.hitCount")
  String viewBreakpointConfigurationHitCount();

  @Key("view.breakpointConfiguration.applyButton")
  String viewBreakpointConfigurationApplyButton();

  @Key("view.breakpointConfiguration.suspend")
  String viewBreakpointConfigurationSuspend();

  @Key("view.breakpointConfiguration.suspend.all")
  String viewBreakpointConfigurationSuspendAll();

  @Key("view.breakpointConfiguration.suspend.thread")
  String viewBreakpointConfigurationSuspendThread();

  @Key("view.breakpointConfiguration.suspend.none")
  String viewBreakpointConfigurationSuspendNone();

  @Key("add.watch.expression")
  String addWatchExpression();

  @Key("remove.watch.expression")
  String removeWatchExpression();

  @Key("addWatchExpressionDescription")
  String addWatchExpressionDescription();

  @Key("removeWatchExpressionDescription")
  String removeWatchExpressionDescription();

  @Key("add.expression.view.dialog.title")
  String addExpressionViewDialogTitle();

  @Key("add.expression.view.save.button.title")
  String addExpressionViewSaveButtonTitle();

  @Key("add.expression.view.cancel.button.title")
  String addExpressionViewCancelButtonTitle();

  @Key("edit.expression.view.dialog.title")
  String editExpressionViewDialogTitle();

  @Key("edit.expression.view.save.button.title")
  String editExpressionViewSaveButtonTitle();

  @Key("edit.expression.view.cancel.button.title")
  String editExpressionViewCancelButtonTitle();

  @Key("edit.expression.view.expression.field.title")
  String editExpressionViewExpressionFieldTitle();

  @Key("action.switch.debugger.displaying.title")
  String switchDebuggerDisplayingTitle();

  @Key("action.switch.debugger.displaying.description")
  String switchDebuggerDisplayingDescription();
}
