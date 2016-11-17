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
package org.eclipse.che.ide.command;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.component.Component;

/**
 * GIN module for configuring Command API components.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(CommandTypeRegistry.class).to(CommandTypeRegistryImpl.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder().build(CommandProducerActionFactory.class));

        GinMapBinder<String, Component> componentsBinder = GinMapBinder.newMapBinder(binder(), String.class, Component.class);
        componentsBinder.addBinding("CommandProducerActionManager").to(CommandProducerActionManager.class);
    }
}
