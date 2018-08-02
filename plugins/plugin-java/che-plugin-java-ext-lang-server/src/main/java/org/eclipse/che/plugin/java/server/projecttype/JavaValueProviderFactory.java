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
package org.eclipse.che.plugin.java.server.projecttype;

import static java.lang.String.valueOf;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.Constants.CONTAINS_JAVA_FILES;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.fs.server.PathTransformer;
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
@Singleton
public class JavaValueProviderFactory implements ValueProviderFactory {

  private final PathTransformer pathTransformer;

  @Inject
  public JavaValueProviderFactory(PathTransformer pathTransformer) {
    this.pathTransformer = pathTransformer;
  }

  @Override
  public ValueProvider newInstance(String wsPath) {
    return new JavaValueProvider(pathTransformer.transform(wsPath));
  }

  private static class JavaValueProvider extends ReadonlyValueProvider {

    /** The root folder of this project. */
    private final Path projectFsPath;
    /** If true, it means that there are some java files in this folder or in its children. */
    private boolean containsJavaFiles;
    /** Try to perform the check on java files only once with lazy check. */
    private boolean initialized = false;

    JavaValueProvider(Path projectFsPath) {
      this.projectFsPath = projectFsPath;
      this.initialized = false;
    }

    /**
     * Check recursively if the given folder contains java files or any of its children
     *
     * @return true if the folder or a subfolder contains java files
     */
    private boolean hasJavaFilesInFolder() {
      try {
        AtomicBoolean detectedJavaFiles = new AtomicBoolean();
        walkFileTree(
            projectFsPath,
            new SimpleFileVisitor<Path>() {
              @Override
              public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                  throws IOException {
                if (file.toFile().getName().endsWith(".java")) {
                  detectedJavaFiles.getAndSet(true);
                  return TERMINATE;
                } else {
                  return CONTINUE;
                }
              }
            });
        return detectedJavaFiles.get();
      } catch (IOException e) {
        throw new IllegalStateException("Unable to get files from from: " + projectFsPath, e);
      }
    }

    /**
     * Checks if java files are available in the root folder or in any children of the root folder
     *
     * @throws ValueStorageException if there is an error when checking
     */
    private void init() throws ValueStorageException {
      try {
        this.containsJavaFiles = hasJavaFilesInFolder();
      } catch (IllegalStateException e) {
        throw new ValueStorageException(
            String.format(
                "Unable to get files from ''%s'' because of %s", projectFsPath, e.getMessage()));
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
