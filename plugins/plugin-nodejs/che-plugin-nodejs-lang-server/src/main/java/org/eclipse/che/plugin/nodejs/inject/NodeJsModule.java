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
package org.eclipse.che.plugin.nodejs.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.nodejs.generator.NodeJsProjectGenerator;
import org.eclipse.che.plugin.nodejs.projecttype.NodeJsProjectType;

import java.nio.file.Path;
import java.nio.file.PathMatcher;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/**
 * @author Dmitry Shnurenko
 */
@DynaModule
public class NodeJsModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<ProjectTypeDef> projectTypeDefMultibinder = Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
        projectTypeDefMultibinder.addBinding().to(NodeJsProjectType.class);

        Multibinder<ProjectHandler> projectHandlerMultibinder = newSetBinder(binder(), ProjectHandler.class);
        projectHandlerMultibinder.addBinding().to(NodeJsProjectGenerator.class);

        configureVfsExcludeFilter();
    }

    private void configureVfsExcludeFilter() {
        newSetBinder(binder(), PathMatcher.class, Names.named("vfs.index_filter_matcher"))
                .addBinding()
                .toInstance(path -> {
                    for (Path pathElement : path) {
                        if (pathElement == null || "node_modules".equals(pathElement.toString())) {
                            return true;
                        }
                    }
                    return false;
                });
    }
}
