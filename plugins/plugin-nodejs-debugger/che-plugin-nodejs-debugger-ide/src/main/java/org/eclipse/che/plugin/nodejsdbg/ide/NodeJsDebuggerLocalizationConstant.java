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
package org.eclipse.che.plugin.nodejsdbg.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n constants for the Debugger extension.
 *
 * @author Anatolii Bazko
 */
public interface NodeJsDebuggerLocalizationConstant extends Messages {

  @Key("view.nodeJsDebuggerConfigurationPage.hostLabel")
  String nodeJsDebuggerConfigurationPageViewHostLabel();

  @Key("view.nodeJsDebuggerConfigurationPage.portLabel")
  String nodeJsDebuggerConfigurationPageViewPortLabel();

  @Key("view.nodeJsDebuggerConfigurationPage.scriptLabel")
  String nodeJsDebuggerConfigurationPageViewScriptLabel();

  @Key("view.nodeJsDebuggerConfigurationPage.pidLabel")
  String nodeJsDebuggerConfigurationPageViewPidLable();
}
