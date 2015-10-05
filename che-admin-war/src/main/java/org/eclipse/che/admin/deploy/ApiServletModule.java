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
package org.eclipse.che.admin.deploy;

import com.google.inject.servlet.ServletModule;

import org.eclipse.che.inject.DynaModule;
import org.everrest.guice.servlet.GuiceEverrestServlet;

/**
 * Defines a servlet module that will redirect all the calls on the Everrest servlet
 * @author Florent Benoit
 */
@DynaModule
public class ApiServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve("/*", new String[0]).with(GuiceEverrestServlet.class);
    }
}
