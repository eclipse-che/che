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
package org.eclipse.che.plugin.maven;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.inject.DynaModule;

/**
 * Binds maven-plugin related properties.
 *
 * @author Yevhenii Voevodin
 */
@DynaModule
public class WsMasterModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), String.class, Names.named("machine.docker.dev_machine.machine_env"))
                   .addBinding()
                   .toProvider(MavenOptsEnvVariableProvider.class);
    }
}
