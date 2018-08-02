/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.python.inject;

import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.python.generator.PythonProjectGenerator;
import org.eclipse.che.plugin.python.languageserver.PythonLanguageSeverConfig;
import org.eclipse.che.plugin.python.projecttype.PythonProjectType;

/** @author Valeriy Svydenko */
@DynaModule
public class PythonModule extends AbstractModule {
  @Override
  protected void configure() {
    newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(PythonProjectType.class);

    newSetBinder(binder(), ProjectHandler.class).addBinding().to(PythonProjectGenerator.class);

    newMapBinder(binder(), String.class, LanguageServerConfig.class)
        .addBinding("org.eclipse.che.plugin.python.languageserver")
        .to(PythonLanguageSeverConfig.class)
        .asEagerSingleton();
  }
}
