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
package org.eclipse.che.ide.ext.java.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathChangedEvent;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;

/**
 * Storage of the classpath entries.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathContainer
    implements ProjectClasspathChangedEvent.ProjectClasspathChangedHandler {
  public static String JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER";

  private final JavaLanguageExtensionServiceClient extensionService;
  private final PromiseProvider promiseProvider;
  private Map<String, Promise<List<ClasspathEntry>>> classpath;

  @Inject
  public ClasspathContainer(
      JavaLanguageExtensionServiceClient extensionService,
      EventBus eventBus,
      PromiseProvider promiseProvider) {
    this.extensionService = extensionService;
    this.promiseProvider = promiseProvider;
    classpath = new HashMap<>();

    eventBus.addHandler(ProjectClasspathChangedEvent.TYPE, this);
  }

  /**
   * Returns list of the classpath entries. If the classpath already exist for this project -
   * returns its otherwise gets classpath from the server.
   *
   * @param projectPath path to the project
   * @return list of the classpath entries
   */
  public Promise<List<ClasspathEntry>> getClasspathEntries(String projectPath) {
    if (classpath.containsKey(projectPath)) {
      return classpath.get(projectPath);
    } else {
      Promise<List<ClasspathEntry>> result =
          extensionService
              .classpathTree(projectPath)
              .catchErrorPromise(
                  error -> {
                    classpath.remove(projectPath);
                    return promiseProvider.reject(error);
                  });
      classpath.put(projectPath, result);
      return result;
    }
  }

  @Override
  public void onProjectClasspathChanged(ProjectClasspathChangedEvent event) {
    classpath.remove(event.getProject());
  }
}
