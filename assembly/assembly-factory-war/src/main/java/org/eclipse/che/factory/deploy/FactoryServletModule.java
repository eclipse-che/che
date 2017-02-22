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
package org.eclipse.che.factory.deploy;

import com.google.inject.servlet.ServletModule;

import org.eclipse.che.factory.filter.FactoryRetrieverFilter;
import org.eclipse.che.factory.filter.RemoveIllegalCharactersFactoryURLFilter;
import org.eclipse.che.inject.DynaModule;

/**
 *  Servlet module composer for factory war.
 *  @author Sergii Kabashniuk
 */
@DynaModule
public class FactoryServletModule extends ServletModule {

    private static final String PASS_RESOURCES_REGEXP = "^(?!/resources/)/?.*$";

    @Override
    protected void configureServlets() {
        filterRegex(PASS_RESOURCES_REGEXP).through(RemoveIllegalCharactersFactoryURLFilter.class);
        filterRegex(PASS_RESOURCES_REGEXP).through(FactoryRetrieverFilter.class);
    }
}
