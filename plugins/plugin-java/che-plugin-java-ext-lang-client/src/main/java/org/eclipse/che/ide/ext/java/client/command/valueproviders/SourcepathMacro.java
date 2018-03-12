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
package org.eclipse.che.ide.ext.java.client.command.valueproviders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.command.ClasspathContainer;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathResolver;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;

/**
 * Provides project's sourcepath value.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SourcepathMacro implements Macro {

  private static final String KEY = "${project.java.sourcepath}";

  private final ClasspathContainer classpathContainer;
  private final ClasspathResolver classpathResolver;
  private final AppContext appContext;
  private final PromiseProvider promises;
  private final JavaLocalizationConstant localizationConstants;

  @Inject
  public SourcepathMacro(
      ClasspathContainer classpathContainer,
      ClasspathResolver classpathResolver,
      AppContext appContext,
      PromiseProvider promises,
      JavaLocalizationConstant localizationConstants) {
    this.classpathContainer = classpathContainer;
    this.classpathResolver = classpathResolver;
    this.appContext = appContext;
    this.promises = promises;
    this.localizationConstants = localizationConstants;
  }

  @Override
  public String getName() {
    return KEY;
  }

  @Override
  public String getDescription() {
    return localizationConstants.macroProjectJavaSourcePathDescription();
  }

  @Override
  public Promise<String> expand() {
    final Resource[] resources = appContext.getResources();

    if (resources == null || resources.length != 1) {
      return promises.resolve("");
    }

    final Resource resource = resources[0];
    final Project project = resource.getProject();

    if (!JavaUtil.isJavaProject(project)) {
      return promises.resolve("");
    }

    final String projectPath = project.getLocation().toString();

    return classpathContainer
        .getClasspathEntries(projectPath)
        .then(
            (Function<List<ClasspathEntry>, String>)
                arg -> {
                  classpathResolver.resolveClasspathEntries(arg);
                  Set<String> sources = classpathResolver.getSources();
                  StringBuilder classpath = new StringBuilder();
                  for (String source : sources) {
                    classpath.append(source.substring(projectPath.length() + 1)).append(':');
                  }

                  if (classpath.toString().isEmpty()) {
                    classpath.append(appContext.getProjectsRoot().toString()).append(projectPath);
                  }

                  return classpath.toString();
                })
        .catchError(PromiseError::getMessage);
  }
}
