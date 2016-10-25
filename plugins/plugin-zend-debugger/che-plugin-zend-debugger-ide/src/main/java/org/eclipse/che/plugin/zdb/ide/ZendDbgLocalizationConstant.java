/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
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
