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

/**
 * Interface to represent the messages contained in resource bundle: 'JavaRuntimeLocalizationConstant.properties'.
 *
 * @author Artem Zatsarynnyi
 */
public interface JavaRuntimeLocalizationConstant extends com.google.gwt.i18n.client.Messages {

    /* Buttons */
    @Key("breakpoints")
    String breakpoints();

    @Key("debug")
    String debug();

    @Key("disconnectButton")
    String disconnectButton();

    @Key("host")
    String host();

    @Key("port")
    String port();

    @Key("removeBreakpointsButton")
    String removeBreakpointsButton();

    @Key("resumeButton")
    String resumeButton();

    @Key("variables")
    String variables();

    @Key("absentInformationVariables")
    String absentInformationVariables();

    @Key("stepInto")
    String stepInto();

    @Key("stepOver")
    String stepOver();

    @Key("stepReturn")
    String stepReturn();

    @Key("changeValue")
    String changeValue();

    @Key("evaluateExpression")
    String evaluateExpression();

    /* messages */
    @Key("debugger.connecting.title")
    String debuggerConnectingTitle(String address);

    @Key("debugger.connected.title")
    String debuggerConnectedTitle();

    @Key("debugger.connected.description")
    String debuggerConnectedDescription(String address);

    @Key("debugger.disconnecting.Title")
    String debuggerDisconnectingTitle();

    @Key("debugger.disconnected.title")
    String debuggerDisconnectedTitle();

    @Key("debugger.disconnected.description")
    String debuggerDisconnectedDescription(String address);

    @Key("failed.to.connect.to.remote.debugger.title")
    String failedToConnectToRemoteDebuggerTitle();

    @Key("failed.to.connect.to.remote.debugger.wrong.port")
    String failedToConnectToRemoteDebuggerWrongPort(String port);

    @Key("failed.to.connect.to.remote.debugger.description")
    String failedToConnectToRemoteDebuggerDescription(String address);

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

    @Key("connect.to.remote.description")
    String connectToRemoteDescription();

    @Key("server.log")
    String displayServerLogTitle();

    @Key("server.log.description")
    String displayServerLogDescription();

    @Key("server.log.tab.title")
    String serverLogTabTitle();

    @Key("view.remoteDebug.description")
    String remoteDebugViewDescription();

}
