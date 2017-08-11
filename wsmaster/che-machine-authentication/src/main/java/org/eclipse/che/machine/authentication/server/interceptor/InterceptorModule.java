/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.machine.authentication.server.interceptor;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.workspace.server.WorkspaceManager;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;


/**
 * Guice interceptor bindings.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
public class InterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
        final MachineTokenInterceptor tokenInterceptor = new MachineTokenInterceptor();
        requestInjection(tokenInterceptor);
        bindInterceptor(subclassesOf(WorkspaceManager.class), names("startWorkspace"), tokenInterceptor);
    }
}
