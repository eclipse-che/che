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

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.PluginGuiceModule;
import org.everrest.guice.servlet.EverrestGuiceContextListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Api Module used to setup guice injection on this web application.
 * @author Florent Benoit
 */
@DynaModule
public class ApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApiInfoService.class);

        install(new PluginGuiceModule());
    }
}
