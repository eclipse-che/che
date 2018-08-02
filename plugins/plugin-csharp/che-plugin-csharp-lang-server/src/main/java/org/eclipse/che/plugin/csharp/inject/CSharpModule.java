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
package org.eclipse.che.plugin.csharp.inject;

import static com.google.inject.multibindings.MapBinder.newMapBinder;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.csharp.languageserver.CSharpLanguageServerConfig;
import org.eclipse.che.plugin.csharp.projecttype.CSharpProjectType;
import org.eclipse.che.plugin.csharp.projecttype.CreateNetCoreProjectHandler;

/** @author Anatolii Bazko */
@DynaModule
public class CSharpModule extends AbstractModule {
  public static final String LANGUAGE_ID = "csharp";

  @Override
  protected void configure() {
    Multibinder<ProjectTypeDef> projectTypeMultibinder =
        Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
    projectTypeMultibinder.addBinding().to(CSharpProjectType.class);

    Multibinder<ProjectHandler> projectHandlersMultibinder =
        Multibinder.newSetBinder(binder(), ProjectHandler.class);
    projectHandlersMultibinder.addBinding().to(CreateNetCoreProjectHandler.class);

    newMapBinder(binder(), String.class, LanguageServerConfig.class)
        .addBinding("org.eclipse.che.plugin.csharp.languageserver")
        .to(CSharpLanguageServerConfig.class)
        .asEagerSingleton();
  }
}
