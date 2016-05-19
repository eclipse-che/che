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

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.java.client.command.ClasspathContainer;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathResolver;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProvider;

import java.util.List;
import java.util.Set;

/**
 * Provides project's sourcepath value.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SourcepathProvider implements CommandPropertyValueProvider {
    private static final String KEY = "${project.java.sourcepath}";

    private final ClasspathContainer classpathContainer;
    private final ClasspathResolver  classpathResolver;
    private final AppContext         appContext;

    @Inject
    public SourcepathProvider(ClasspathContainer classpathContainer,
                              ClasspathResolver classpathResolver,
                              AppContext appContext) {
        this.classpathContainer = classpathContainer;
        this.classpathResolver = classpathResolver;
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
            return Promises.resolve("");
        }

        final String projectPath = currentProject.getProjectConfig().getPath();

        return classpathContainer.getClasspathEntries(projectPath).then(
                new Function<List<ClasspathEntryDto>, String>() {
                    @Override
                    public String apply(List<ClasspathEntryDto> arg) throws FunctionException {
                        classpathResolver.resolveClasspathEntries(arg);
                        Set<String> sources = classpathResolver.getSources();
                        StringBuilder sourcepath = new StringBuilder("");
                        for (String source : sources) {
                            sourcepath.append(source.substring(projectPath.length() + 1));
                        }

                        if (sourcepath.toString().isEmpty()) {
                            sourcepath.append(appContext.getProjectsRoot()).append(projectPath);
                        }

                        sourcepath.append(':');

                        return sourcepath.toString();
                    }
                });
    }

}
