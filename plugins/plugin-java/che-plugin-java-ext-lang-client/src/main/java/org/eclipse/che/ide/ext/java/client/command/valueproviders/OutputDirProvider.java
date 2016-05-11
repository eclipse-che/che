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
package org.eclipse.che.ide.ext.java.client.command.valueproviders;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProvider;

/**
 * Provides a path to the project's output directory.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class OutputDirProvider implements CommandPropertyValueProvider {
    private static final String KEY = "${project.java.output.dir}";

    private final AppContext         appContext;

    @Inject
    public OutputDirProvider(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public Promise<String> getValue() {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            return Promises.resolve("");
        }

        String languageAttribute = currentProject.getAttributeValue(Constants.LANGUAGE);
        if (!Constants.JAVA_ID.equals(languageAttribute)) {
            return Promises.resolve(appContext.getProjectsRoot() + currentProject.getProjectConfig().getPath());
        }

        return Promises.resolve(appContext.getProjectsRoot() + currentProject.getAttributeValue(Constants.OUTPUT_FOLDER));
    }

}
