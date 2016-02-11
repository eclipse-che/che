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
package org.eclipse.che.ide.ext.web;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'WebLocalizationConstant.properties'.
 *
 * @author Artem Zatsarynnyi
 */
public interface WebLocalizationConstant extends Messages {
    /* Actions */
    @Key("action.newCssFile.id")
    String newCssFileActionId();

    @Key("action.newCssFile.title")
    String newCssFileActionTitle();

    @Key("action.newCssFile.description")
    String newCssFileActionDescription();

    @Key("action.newLessFile.id")
    String newLessFileActionId();

    @Key("action.newLessFile.title")
    String newLessFileActionTitle();

    @Key("action.newLessFile.description")
    String newLessFileActionDescription();

    @Key("action.newHtmlFile.id")
    String newHtmlFileActionId();

    @Key("action.newHtmlFile.title")
    String newHtmlFileActionTitle();

    @Key("action.newHtmlFile.description")
    String newHtmlFileActionDescription();

    @Key("action.newJavaScriptFile.id")
    String newJavaScriptFileActionId();

    @Key("action.newJavaScriptFile.title")
    String newJavaScriptFileActionTitle();

    @Key("action.newJavaScriptFile.description")
    String newJavaScriptFileActionDescription();
}
