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
package org.eclipse.che.plugin.web.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.web.shared.Constants;
import org.eclipse.che.plugin.web.typescript.TSLSLauncher;
import org.eclipse.che.plugin.web.typescript.TypeScriptProjectType;

import static java.util.Arrays.asList;

/**
 * The module that contains configuration of the server side part of the Web plugin
 */
@DynaModule
public class WebModule extends AbstractModule {

    private static final String[] EXTENSIONS = new String[]{Constants.TS_EXT};
    private static final String MIME_TYPE = Constants.TS_MIME_TYPE;

   @Override
    protected void configure() {
        Multibinder<ProjectTypeDef> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
        projectTypeMultibinder.addBinding().to(TypeScriptProjectType.class);

        Multibinder.newSetBinder(binder(), LanguageServerLauncher.class).addBinding().to(TSLSLauncher.class);
        LanguageDescription description = new LanguageDescription();
        description.setFileExtensions(asList(EXTENSIONS));
        description.setLanguageId(Constants.TS_LANG);
        description.setMimeType(MIME_TYPE);
        description.setHighlightingConfiguration("[\n" +
                                                 "  {\"include\":\"orion.js\"},\n" +
                                                 "  {\"match\":\"\\\\b(?:constructor|declare|module)\\\\b\",\"name\" :\"keyword.operator.typescript\"},\n" +
                                                 "  {\"match\":\"\\\\b(?:any|boolean|number|string)\\\\b\",\"name\" : \"storage.type.typescript\"}\n" +
                                                 "]");
        Multibinder.newSetBinder(binder(), LanguageDescription.class).addBinding().toInstance(description);
    }
}
