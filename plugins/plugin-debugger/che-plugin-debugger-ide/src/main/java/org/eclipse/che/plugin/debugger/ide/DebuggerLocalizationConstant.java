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

    @Key("suspend")
    String suspend();

    @Key("changeVariableValue")
    String changeVariableValue();

    @Key("evaluateExpression")
    String evaluateExpression();

    @Key("showHideDebuggerPanel")
    String showHideDebuggerPanel();

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

    @Key("suspendDescription")
    String suspendDescription();

    @Key("deleteAllBreakpointsDescription")
    String deleteAllBreakpointsDescription();

    @Key("changeVariableValueDescription")
    String changeVariableValueDescription();

    @Key("evaluateExpressionDescription")
    String evaluateExpressionDescription();

    @Key("showHideDebuggerPanelDescription")
    String showHideDebuggerPanelDescription();

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

    @Key("failed.to.get.variable.value.title")
    String failedToGetVariableValueTitle();

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
}
