/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.ide;

/**
 * I18n constants for the Zend Debugger extension.
 *
 * @author Bartlomiej Laczkowski
 */
public interface ZendDebuggerLocalizationConstant extends com.google.gwt.i18n.client.Messages {

    /* JavaDebugConfigurationPage */
    @Key("view.zendDebugConfigurationPage.notice")
    String zendDebugConfigurationPageViewNotice();

    @Key("view.zendDebugConfigurationPage.clientHostIPLabel")
    String zendDebugConfigurationPageViewClientHostIPLabel();

    @Key("view.zendDebugConfigurationPage.debugPortLabel")
    String zendDebugConfigurationPageViewDebugPortLabel();
    
    @Key("view.zendDebugConfigurationPage.breakAtFirstLineCheckbox")
    String zendDebugConfigurationPageViewBreakAtFirstLineCheckbox();
    
    @Key("view.zendDebugConfigurationPage.useSslEncryptionCheckbox")
    String zendDebugConfigurationPageViewUseSslEncryptionCheckbox();
    
}
