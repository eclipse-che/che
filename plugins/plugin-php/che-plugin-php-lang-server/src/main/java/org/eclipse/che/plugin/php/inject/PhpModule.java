/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.php.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.php.languageserver.PhpLanguageServerLauncher;
import org.eclipse.che.plugin.php.projecttype.PhpProjectGenerator;
import org.eclipse.che.plugin.php.projecttype.PhpProjectType;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/**
 * @author Kaloyan Raev
 */
@DynaModule
public class PhpModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<ProjectTypeDef> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
        projectTypeMultibinder.addBinding().to(PhpProjectType.class);

        Multibinder<ProjectHandler> projectHandlerMultibinder = newSetBinder(binder(), ProjectHandler.class);
        projectHandlerMultibinder.addBinding().to(PhpProjectGenerator.class);

        Multibinder.newSetBinder(binder(), LanguageServerLauncher.class).addBinding().to(PhpLanguageServerLauncher.class);
    }
}
