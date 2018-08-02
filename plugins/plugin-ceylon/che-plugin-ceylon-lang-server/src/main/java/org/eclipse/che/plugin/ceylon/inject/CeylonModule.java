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
package org.eclipse.che.plugin.ceylon.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.ceylon.projecttype.CeylonProjectType;
import org.eclipse.che.plugin.ceylon.projecttype.CreateCeylonProjectHandler;

/** @author David Festal */
@DynaModule
public class CeylonModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<ProjectTypeDef> projectTypeMultibinder =
        Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
    projectTypeMultibinder.addBinding().to(CeylonProjectType.class);

    Multibinder<ProjectHandler> projectHandlersMultibinder =
        Multibinder.newSetBinder(binder(), ProjectHandler.class);
    projectHandlersMultibinder.addBinding().to(CreateCeylonProjectHandler.class);
  }
}
