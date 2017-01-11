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
package org.eclipse.che.plugin.cpp.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.cpp.generator.CProjectGenerator;
import org.eclipse.che.plugin.cpp.generator.CppProjectGenerator;
import org.eclipse.che.plugin.cpp.projecttype.CProjectType;
import org.eclipse.che.plugin.cpp.projecttype.CppProjectType;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/**
 * @author Vitaly Parfonov
 */
@DynaModule
public class CppModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<ProjectTypeDef> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
        projectTypeMultibinder.addBinding().to(CProjectType.class);
        projectTypeMultibinder.addBinding().to(CppProjectType.class);

        Multibinder<ProjectHandler> projectHandlerMultibinder = newSetBinder(binder(), ProjectHandler.class);
        projectHandlerMultibinder.addBinding().to(CppProjectGenerator.class);
        projectHandlerMultibinder.addBinding().to(CProjectGenerator.class);
    }
}
