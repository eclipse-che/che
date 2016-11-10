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
package org.eclipse.che.ide.ext.machine.server;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.workspace.server.RecipeScriptDownloadService;
import org.eclipse.che.ide.ext.machine.server.ssh.KeysInjector;
import org.eclipse.che.ide.ext.machine.server.ssh.WorkspaceSshKeys;
import org.eclipse.che.inject.DynaModule;

@DynaModule
public class MachineModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KeysInjector.class).asEagerSingleton();
        bind(WorkspaceSshKeys.class).asEagerSingleton();
        bind(RecipeScriptDownloadService.class);
    }
}
