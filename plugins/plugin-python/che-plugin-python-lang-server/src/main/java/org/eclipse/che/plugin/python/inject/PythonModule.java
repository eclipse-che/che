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
package org.eclipse.che.plugin.python.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.python.generator.PythonProjectGenerator;
import org.eclipse.che.plugin.python.languageserver.PythonLanguageSeverLauncher;
import org.eclipse.che.plugin.python.projecttype.PythonProjectType;
import org.eclipse.che.plugin.python.shared.ProjectAttributes;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static java.util.Arrays.asList;

/**
 * @author Valeriy Svydenko
 */
@DynaModule
public class PythonModule extends AbstractModule {
    private static final String[] EXTENSIONS = new String[]{ProjectAttributes.PYTHON_EXT};
    private static final String MIME_TYPE = "text/x-python";

    @Override
    protected void configure() {
        Multibinder<ProjectTypeDef> projectTypeMultibinder = newSetBinder(binder(), ProjectTypeDef.class);
        projectTypeMultibinder.addBinding().to(PythonProjectType.class);

        Multibinder<ProjectHandler> projectHandlerMultibinder = newSetBinder(binder(), ProjectHandler.class);
        projectHandlerMultibinder.addBinding().to(PythonProjectGenerator.class);

        Multibinder.newSetBinder(binder(), LanguageServerLauncher.class).addBinding().to(PythonLanguageSeverLauncher.class);
        LanguageDescription description = new LanguageDescription();
        description.setFileExtensions(asList(EXTENSIONS));
        description.setLanguageId(ProjectAttributes.PYTHON_ID);
        description.setMimeType(MIME_TYPE);
        Multibinder.newSetBinder(binder(), LanguageDescription.class).addBinding().toInstance(description);
   }
}
