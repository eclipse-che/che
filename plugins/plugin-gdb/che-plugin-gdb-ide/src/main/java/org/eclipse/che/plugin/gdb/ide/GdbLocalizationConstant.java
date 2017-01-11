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
package org.eclipse.che.plugin.gdb.ide;

/**
 * I18n constants for the Debugger extension.
 *
 * @author Artem Zatsarynnyi
 */
public interface GdbLocalizationConstant extends com.google.gwt.i18n.client.Messages {

    /* GdbConfigurationPage */
    @Key("view.gdbConfigurationPage.hostLabel")
    String gdbConfigurationPageViewHostLabel();

    @Key("view.gdbConfigurationPage.portLabel")
    String gdbConfigurationPageViewPortLabel();

    @Key("view.gdbConfigurationPage.binPathLabel")
    String gdbConfigurationPageViewBinPathLabel();

    @Key("view.gdbConfigurationPage.binPathDescription")
    String gdbConfigurationPageViewBinPathDescription();

    @Key("view.gdbConfigurationPage.devHostCheckbox")
    String gdbConfigurationPageViewDevMachineCheckbox();

    @Key("gdbDebugger.message.suspendToActivateBreakpoints")
    String messageSuspendToActivateBreakpoints();
}
