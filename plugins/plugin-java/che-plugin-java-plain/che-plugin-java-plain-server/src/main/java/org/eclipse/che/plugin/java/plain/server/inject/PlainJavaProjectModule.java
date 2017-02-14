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
package org.eclipse.che.plugin.java.plain.server.inject;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.java.plain.server.generator.PlainJavaProjectGenerator;
import org.eclipse.che.plugin.java.plain.server.projecttype.PlainJavaInitHandler;
import org.eclipse.che.plugin.java.plain.server.projecttype.PlainJavaProjectType;
import org.eclipse.che.plugin.java.plain.server.rest.ClasspathUpdaterService;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/**
 * @author Valeriy Svydenko
 */
@DynaModule
public class PlainJavaProjectModule extends AbstractModule {
    @Override
    protected void configure() {
        newSetBinder(binder(), ProjectTypeDef.class).addBinding().to(PlainJavaProjectType.class);
        newSetBinder(binder(), ProjectHandler.class).addBinding().to(PlainJavaProjectGenerator.class);
        newSetBinder(binder(), ProjectHandler.class).addBinding().to(PlainJavaInitHandler.class);

        bind(ClasspathUpdaterService.class);
    }
}
