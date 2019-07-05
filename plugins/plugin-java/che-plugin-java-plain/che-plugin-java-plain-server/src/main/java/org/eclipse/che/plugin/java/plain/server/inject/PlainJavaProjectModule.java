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
package org.eclipse.che.plugin.java.plain.server.inject;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.java.plain.server.ProjectsListener;
import org.eclipse.che.plugin.java.plain.server.generator.PlainJavaProjectGenerator;
import org.eclipse.che.plugin.java.plain.server.projecttype.PlainJavaInitHandler;
import org.eclipse.che.plugin.java.plain.server.projecttype.PlainJavaProjectType;
import org.eclipse.che.plugin.java.plain.server.rest.ClasspathUpdaterService;

/** @author Valeriy Svydenko */
@DynaModule
public class PlainJavaProjectModule extends AbstractModule {
  @Override
  protected void configure() {
    newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(PlainJavaProjectType.class);
    newSetBinder(binder(), ProjectHandler.class).addBinding().to(PlainJavaProjectGenerator.class);
    newSetBinder(binder(), ProjectHandler.class).addBinding().to(PlainJavaInitHandler.class);

    bind(ClasspathUpdaterService.class);
    bind(PlainJavaProjectSourceFolderWatcher.class).asEagerSingleton();
    bind(ProjectsListener.class).asEagerSingleton();
  }
}
