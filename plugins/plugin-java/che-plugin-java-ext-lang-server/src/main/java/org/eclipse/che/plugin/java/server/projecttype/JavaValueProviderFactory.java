/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.projecttype;

import static java.lang.String.valueOf;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.Constants.CONTAINS_JAVA_FILES;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.ide.ext.java.shared.Constants;

/**
 * {@link ValueProviderFactory} for Java project type. Factory creates a class which provides values
 * of Java project's attributes.
 *
 * @author gazarenkov
 * @author Florent Benoit
 */
public class JavaValueProviderFactory implements ValueProviderFactory {

  @Override
  public ValueProvider newInstance(ProjectConfig projectConfig) {
    return new JavaValueProvider(projectConfig);
  }

  static class JavaValueProvider extends ReadonlyValueProvider {

    /** The root folder of this project. */
    private final String projectWsPath;
    /** If true, it means that there are some java files in this folder or in its children. */
    private boolean containsJavaFiles;
    /** Try to perform the check on java files only once with lazy check. */
    private boolean initialized = false;

    public JavaValueProvider(ProjectConfig projectConfig) {
      this.projectWsPath = projectConfig.getPath();
      this.initialized = false;
    }

    /**
     * Check recursively if the given folder contains java files or any of its children
     *
     * @param projectWsPath the initial folder to check
     * @return true if the folder or a subfolder contains java files
     */
    protected boolean hasJavaFilesInFolder(final String projectWsPath) {
      try {
        Path start = Paths.get("/projects/" + projectWsPath);
        AtomicBoolean hasJavaFilesInFolder = new AtomicBoolean();
        Files.walkFileTree(
            start,
            new SimpleFileVisitor<Path>() {
              @Override
              public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                  throws IOException {
                if (file.getFileName().endsWith(".java")) {
                  hasJavaFilesInFolder.getAndSet(true);
                  return TERMINATE;
                } else {
                  return CONTINUE;
                }
              }
            });
        return hasJavaFilesInFolder.get();
      } catch (IOException e) {
        throw new IllegalStateException(
            String.format("Unable to get files from ''%s''", projectWsPath), e);
      }
    }

    /**
     * Checks if java files are available in the root folder or in any children of the root folder
     *
     * @throws ValueStorageException if there is an error when checking
     */
    protected void init() throws ValueStorageException {
      try {
        this.containsJavaFiles = hasJavaFilesInFolder(projectWsPath);
      } catch (IllegalStateException e) {
        throw new ValueStorageException(
            String.format("Unable to get files from ''%s''", projectWsPath) + e.getMessage());
      }
      this.initialized = true;
    }

    @Override
    public List<String> getValues(String attributeName) throws ValueStorageException {
      if (!initialized) {
        init();
      }
      if (attributeName.equals(Constants.LANGUAGE_VERSION)) {
        return singletonList(System.getProperty("java.version"));
      } else if (CONTAINS_JAVA_FILES.equals(attributeName)) {
        return singletonList(valueOf(containsJavaFiles));
      }
      return null;
    }
  }
}
