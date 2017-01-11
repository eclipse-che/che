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
package org.eclipse.che.api.debugger.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.debug.shared.dto.action.ActionDto;

/**
 * The module that contains configuration of the server side part of the Debugger.
 *
 * @author Anatoliy Bazko
 */
public class DebuggerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DebuggerManager.class);
        bind(DebuggerService.class);
        bind(DebuggerWebSocketMessenger.class);

        bind(DebuggerActionProvider.class);
        final Multibinder<Class> ignoredClasses = Multibinder.newSetBinder(binder(), Class.class, Names.named("che.json.ignored_classes"));
        ignoredClasses.addBinding().toInstance(ActionDto.class);
    }
}
