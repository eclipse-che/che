/*
 * Copyright (c) 2016-2017 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.composer.server;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.composer.server.executor.ComposerJsonRpcMessenger;
import org.eclipse.che.plugin.composer.server.projecttype.ComposerProjectGenerator;
import org.eclipse.che.plugin.composer.server.projecttype.ComposerProjectInitializer;
import org.eclipse.che.plugin.composer.server.projecttype.ComposerProjectType;
import org.eclipse.che.plugin.composer.server.projecttype.ComposerValueProviderFactory;

/**
 * The module that contains configuration of the server side part of the Composer extension.
 *
 * @author Kaloyan Raev
 */
@DynaModule
public class ComposerModule extends AbstractModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    Multibinder<ProjectTypeDef> projectTypeMultibinder =
        newSetBinder(binder(), ProjectTypeDef.class);
    projectTypeMultibinder.addBinding().to(ComposerProjectType.class);

    Multibinder<ValueProviderFactory> valueProviderMultibinder =
        newSetBinder(binder(), ValueProviderFactory.class);
    valueProviderMultibinder.addBinding().to(ComposerValueProviderFactory.class);

    Multibinder<ProjectHandler> projectHandlerMultibinder =
        newSetBinder(binder(), ProjectHandler.class);
    projectHandlerMultibinder.addBinding().to(ComposerProjectGenerator.class);
    projectHandlerMultibinder.addBinding().to(ComposerProjectInitializer.class);

    bind(ComposerJsonRpcMessenger.class);
  }
}
