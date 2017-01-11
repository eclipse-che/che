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
package org.eclipse.che.plugin.jdb.ide;

/**
 * I18n constants for the Debugger extension.
 *
 * @author Artem Zatsarynnyi
 */
public interface JavaDebuggerLocalizationConstant extends com.google.gwt.i18n.client.Messages {

    /* JavaDebugConfigurationPage */
    @Key("view.javaDebugConfigurationPage.notice")
    String javaDebugConfigurationPageViewNotice();

    @Key("view.javaDebugConfigurationPage.devHostCheckbox")
    String javaDebugConfigurationPageViewDevHostCheckbox();

    @Key("view.javaDebugConfigurationPage.hostLabel")
    String javaDebugConfigurationPageViewHostLabel();

    @Key("view.javaDebugConfigurationPage.portLabel")
    String javaDebugConfigurationPageViewPortLabel();
}
