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
package org.eclipse.che.plugin.web.inject;

import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.web.typescript.TypeScriptLanguageServerConfig;
import org.eclipse.che.plugin.web.typescript.TypeScriptProjectType;
import org.eclipse.che.plugin.web.vue.VueLanguageServerConfig;
import org.eclipse.che.plugin.web.vue.VueProjectType;

/** The module that contains configuration of the server side part of the Web plugin */
@DynaModule
public class WebModule extends AbstractModule {
  @Override
  protected void configure() {
    // TypeScript Configuration
    newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(TypeScriptProjectType.class);

    newMapBinder(binder(), String.class, LanguageServerConfig.class)
        .addBinding("org.eclipse.che.plugin.web.typescript")
        .to(TypeScriptLanguageServerConfig.class)
        .asEagerSingleton();

    // Vue Configuration
    newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(VueProjectType.class);

    newMapBinder(binder(), String.class, LanguageServerConfig.class)
        .addBinding("org.eclipse.che.plugin.web.vue")
        .to(VueLanguageServerConfig.class)
        .asEagerSingleton();
  }
}
