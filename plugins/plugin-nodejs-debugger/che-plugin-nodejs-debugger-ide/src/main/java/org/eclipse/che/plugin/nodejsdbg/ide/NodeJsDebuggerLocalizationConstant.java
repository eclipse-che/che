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
