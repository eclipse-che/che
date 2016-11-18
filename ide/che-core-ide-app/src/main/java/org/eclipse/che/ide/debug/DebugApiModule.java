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
package org.eclipse.che.ide.debug;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.BreakpointRenderer;
import org.eclipse.che.ide.api.debug.BreakpointRendererFactory;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.debug.DebuggerServiceClientImpl;

/**
 * GIN module for configuring Debug API components.
 *
 * @author Artem Zatsarynnyi
 */
public class DebugApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(DebuggerServiceClient.class).to(DebuggerServiceClientImpl.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder()
                        .implement(BreakpointRenderer.class, BreakpointRendererImpl.class)
                        .build(BreakpointRendererFactory.class));

        bind(BreakpointManager.class).to(BreakpointManagerImpl.class).in(Singleton.class);
    }
}
