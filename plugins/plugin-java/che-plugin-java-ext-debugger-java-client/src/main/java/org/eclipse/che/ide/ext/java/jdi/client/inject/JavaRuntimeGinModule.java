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
package org.eclipse.che.ide.ext.java.jdi.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.debugger.client.debug.DebuggerServiceClient;
import org.eclipse.che.ide.ext.java.jdi.client.debug.JavaDebuggerServiceClientImpl;

/** @author Andrey Plotnikov */
@ExtensionGinModule
public class JavaRuntimeGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(DebuggerServiceClient.class).to(JavaDebuggerServiceClientImpl.class).in(Singleton.class);
    }
}
