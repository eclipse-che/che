/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
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
