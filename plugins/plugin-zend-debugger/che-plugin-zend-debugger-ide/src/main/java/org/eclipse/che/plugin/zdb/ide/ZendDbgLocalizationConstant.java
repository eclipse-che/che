/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.ide;

/**
 * I18n constants for the Zend Debugger extension.
 *
 * @author Bartlomiej Laczkowski
 */
public interface ZendDbgLocalizationConstant extends com.google.gwt.i18n.client.Messages {

  @Key("view.zendDbgConfigurationPage.notice")
  String zendDbgConfigurationPageViewNotice();

  @Key("view.zendDbgConfigurationPage.clientHostIPLabel")
  String zendDbgConfigurationPageViewClientHostIPLabel();

  @Key("view.zendDbgConfigurationPage.debugPortLabel")
  String zendDbgConfigurationPageViewDebugPortLabel();

  @Key("view.zendDbgConfigurationPage.breakAtFirstLineCheckbox")
  String zendDbgConfigurationPageViewBreakAtFirstLineCheckbox();

  @Key("view.zendDbgConfigurationPage.useSslEncryptionCheckbox")
  String zendDbgConfigurationPageViewUseSslEncryptionCheckbox();
}
