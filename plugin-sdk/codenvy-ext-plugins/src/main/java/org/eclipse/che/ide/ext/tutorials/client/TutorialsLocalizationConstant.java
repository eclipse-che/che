/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.tutorials.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface represents the constants contained in resource bundle:
 * 'TutorialsLocalizationConstant.properties'.
 *
 * @author Artem Zatsarynnyy
 */
public interface TutorialsLocalizationConstant extends Messages {
    /* Actions */
    @Key("control.showTutorialGuide.id")
    String showTutorialGuideActionId();

    @Key("control.showTutorialGuide.text")
    String showTutorialGuideActionText();

    @Key("control.showTutorialGuide.description")
    String showTutorialGuideActionDescription();

    @Key("control.updateExtension.id")
    String updateExtensionActionId();

    @Key("control.updateExtension.text")
    String updateExtensionText();

    @Key("control.updateExtension.description")
    String updateExtensionDescription();

    @Key("appUpdating")
    String applicationUpdating(String name);

    @Key("appUpdated")
    String applicationUpdated(String name);

    @Key("updateAppFailed")
    String updateApplicationFailed(String name);
}
