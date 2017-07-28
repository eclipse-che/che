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
package org.eclipse.che.api.deploy;

import com.google.inject.servlet.ServletModule;

import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.machine.authentication.server.MachineLoginFilter;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com).
 */
@DynaModule
public class MachineAuthServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        // Not contains '/websocket/' and not ends with '/ws' or '/eventbus'
        filterRegex("^(?!.*/websocket/)(?!.*(/ws|/eventbus)$).*").through(MachineLoginFilter.class);
    }
}
