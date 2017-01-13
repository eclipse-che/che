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
package org.eclipse.che.ide.ext.plugins.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.plugins.client.command.GwtCheCommandType;
import org.eclipse.che.ide.api.command.CommandType;

/**
 * GIN module for 'Che Plugins Development' extension.
 *
 * @author Artem Zatsarynnyi
 */
@ExtensionGinModule
public class PluginsGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), CommandType.class).addBinding().to(GwtCheCommandType.class);
    }
}
