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
package org.eclipse.che.plugin.nodejs.inject;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.nodejs.generator.NodeJsProjectGenerator;
import org.eclipse.che.plugin.nodejs.projecttype.NodeJsProjectType;

/** @author Dmitry Shnurenko */
@DynaModule
public class NodeJsModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<ProjectTypeDef> projectTypeDefMultibinder =
        Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
    projectTypeDefMultibinder.addBinding().to(NodeJsProjectType.class);

    Multibinder<ProjectHandler> projectHandlerMultibinder =
        newSetBinder(binder(), ProjectHandler.class);
    projectHandlerMultibinder.addBinding().to(NodeJsProjectGenerator.class);

    configureVfsExcludeFilter();
  }

  private void configureVfsExcludeFilter() {
    newSetBinder(binder(), PathMatcher.class, Names.named("vfs.index_filter_matcher"))
        .addBinding()
        .toInstance(
            path -> {
              for (Path pathElement : path) {
                if (pathElement == null || "node_modules".equals(pathElement.toString())) {
                  return true;
                }
              }
              return false;
            });
  }
}
