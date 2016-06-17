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
package org.eclipse.che.plugin.languageserver.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Evgen Vidolob
 */
public interface LanguageServerLocalization extends Messages {

    @Key("go.to.symbol.action.title")
    String goToSymbolActionTitle();

    @Key("go.to.symbol.action.description")
    String goToSymbolActionDescription();

    @Key("go.to.symbol.symbols")
    String goToSymbolSymbols(int num);

    @Key("modules.type")
    String modulesType(int p0);

    @Key("class.type")
    String classType(int p0);

    @Key("interface.type")
    String interfaceType(int p0);

    @Key("method.type")
    String methodType(int p0);

    @Key("function.type")
    String functionType(int p0);

    @Key("property.type")
    String propertyType(int p0);

    @Key("variable.type")
    String variableType(int p0);

    @Key("constructor.type")
    String constructorType(int p0);

    @Key("find.symbol.action.title")
    String findSymbolActionTitle();
}
