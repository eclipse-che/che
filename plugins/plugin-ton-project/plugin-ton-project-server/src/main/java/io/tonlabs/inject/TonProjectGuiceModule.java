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
package io.tonlabs.inject;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.tonlabs.generator.TonProjectCreateProjectHandler;
import io.tonlabs.projecttype.TonProjectProjectType;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;

/** TON Project Guice module for setting up project type, project wizard and service bindings. */
@DynaModule
public class TonProjectGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<ProjectTypeDef> projectTypeDefMultibinder =
        newSetBinder(this.binder(), ProjectTypeDef.class);
    projectTypeDefMultibinder.addBinding().to(TonProjectProjectType.class);

    Multibinder<ProjectHandler> projectHandlerMultibinder =
        newSetBinder(this.binder(), ProjectHandler.class);
    projectHandlerMultibinder.addBinding().to(TonProjectCreateProjectHandler.class);
  }
}
