/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.composer.server.projecttype.ComposerProjectGenerator;
import org.eclipse.che.plugin.composer.server.projecttype.ComposerProjectInitializer;
import org.eclipse.che.plugin.composer.server.projecttype.ComposerProjectType;
import org.eclipse.che.plugin.composer.server.projecttype.ComposerValueProviderFactory;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/**
 * The module that contains configuration of the server side part of the
 * Composer extension.
 *
 * @author Kaloyan Raev
 */
@DynaModule
public class ComposerModule extends AbstractModule {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        Multibinder<ProjectTypeDef> projectTypeMultibinder = newSetBinder(binder(), ProjectTypeDef.class);
        projectTypeMultibinder.addBinding().to(ComposerProjectType.class);

        Multibinder<ValueProviderFactory> valueProviderMultibinder = newSetBinder(binder(), ValueProviderFactory.class);
        valueProviderMultibinder.addBinding().to(ComposerValueProviderFactory.class);

        Multibinder<ProjectHandler> projectHandlerMultibinder = newSetBinder(binder(), ProjectHandler.class);
        projectHandlerMultibinder.addBinding().to(ComposerProjectGenerator.class);
        projectHandlerMultibinder.addBinding().to(ComposerProjectInitializer.class);
    }
}
