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
package org.eclipse.che.ide.ext.java.testing.testng.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'TestNGLocalizationConstant.properties'.
 *
 * @author Mirage Abeysekara
 */
public interface TestNGLocalizationConstant extends Messages {

    @Key("action.runClass.title")
    String actionRunClassTitle();

    @Key("action.runClass.description")
    String actionRunClassDescription();

    @Key("action.runClassContext.title")
    String actionRunClassContextTitle();

    @Key("action.runClassContext.description")
    String actionRunClassContextDescription();

    @Key("action.runAll.title")
    String actionRunAllTitle();

    @Key("action.runAll.description")
    String actionRunAllDescription();

    @Key("action.runXML.title")
    String actionRunXMLTitle();

    @Key("action.runXML.description")
    String actionRunXMLDescription();
}
