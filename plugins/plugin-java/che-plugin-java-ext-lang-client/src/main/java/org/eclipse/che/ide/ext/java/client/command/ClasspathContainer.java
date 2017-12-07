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
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathChangedEvent;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;

/**
 * Storage of the classpath entries.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathContainer implements ClasspathChangedEvent.ClasspathChangedHandler {
  public static String JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER";

  private final JavaLanguageExtensionServiceClient extensionService;
  private Map<String, Promise<List<ClasspathEntryDto>>> classpathes;

  @Inject
  public ClasspathContainer(
      JavaLanguageExtensionServiceClient extensionService, EventBus eventBus) {
    this.extensionService = extensionService;
    classpathes = new HashMap<>();

    eventBus.addHandler(ClasspathChangedEvent.TYPE, this);
  }

  /**
   * Returns list of the classpath entries. If the classpath already exist for this project -
   * returns its otherwise gets classpath from the server.
   *
   * @param projectPath path to the project
   * @return list of the classpath entries
   */
  public Promise<List<ClasspathEntryDto>> getClasspathEntries(String projectPath) {
    if (classpathes.containsKey(projectPath)) {
      return classpathes.get(projectPath);
    } else {
      Promise<List<ClasspathEntryDto>> result = extensionService.classpathTree(projectPath);
      classpathes.put(projectPath, result);
      return result;
    }
  }

  @Override
  public void onClasspathChanged(ClasspathChangedEvent event) {
    classpathes.put(event.getPath(), Promises.resolve(event.getEntries()));
  }
}
