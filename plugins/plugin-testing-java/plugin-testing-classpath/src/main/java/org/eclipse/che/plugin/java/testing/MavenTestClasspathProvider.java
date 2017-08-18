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
package org.eclipse.che.plugin.java.testing;

import com.google.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.plugin.java.server.rest.ClasspathServiceInterface;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Maven implementation for the test classpath provider.
 *
 * @author Mirage Abeysekara
 * @author David Festal
 */
public class MavenTestClasspathProvider implements TestClasspathProvider {
  private ClasspathServiceInterface classpathService;

  @Inject
  public MavenTestClasspathProvider(ClasspathServiceInterface classpathService) {
    this.classpathService = classpathService;
  }

  /** {@inheritDoc} */
  @Override
  public ClassLoader getClassLoader(
      String projectAbsolutePath, String projectRelativePath, boolean updateClasspath)
      throws Exception {
    try {
      return new URLClassLoader(
          getProjectClasspath(projectAbsolutePath, projectRelativePath, getWorkspaceRoot()), null);
    } catch (JavaModelException e) {
      throw new Exception(
          "Failed to build the classpath for testing project: " + projectRelativePath, e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String getProjectType() {
    return "maven";
  }

  private Stream<ClasspathEntryDto> toResolvedClassPath(Stream<ClasspathEntryDto> rawClasspath) {
    return rawClasspath.flatMap(
        dto -> {
          if (dto.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
            return toResolvedClassPath(dto.getExpandedEntries().stream());
          } else {
            return Stream.of(dto);
          }
        });
  }

  private IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

  public URL[] getProjectClasspath(
      String projectAbsolutePath, String projectRelativePath, IWorkspaceRoot root)
      throws JavaModelException {
    Stream<ClasspathEntryDto> rawClasspath =
        classpathService.getClasspath(projectRelativePath).stream();
    Stream<ClasspathEntryDto> resolvedClasspath = toResolvedClassPath(rawClasspath);
    return resolvedClasspath
        .map(
            dto -> {
              try {
                String dtoPath = dto.getPath();
                IResource res = root.findMember(new Path(dtoPath));
                File path;
                switch (dto.getEntryKind()) {
                  case IClasspathEntry.CPE_LIBRARY:
                    if (res == null) {
                      path = new File(dtoPath);
                    } else {
                      path = res.getLocation().toFile();
                    }
                    break;
                  case IClasspathEntry.CPE_SOURCE:
                    IPath relativePathFromProjectRoot = new Path(dtoPath).removeFirstSegments(1);
                    String relativePathFromProjectRootStr = relativePathFromProjectRoot.toString();
                    switch (relativePathFromProjectRootStr) {
                      case "src/main/java":
                        path = Paths.get(projectAbsolutePath, "target", "classes").toFile();
                        break;
                      case "src/test/java":
                        path = Paths.get(projectAbsolutePath, "target", "test-classes").toFile();
                        break;
                      default:
                        path = Paths.get(projectAbsolutePath, "target", "classes").toFile();
                    }
                    break;
                  default:
                    path = new File(dtoPath);
                }
                return path.toURI().toURL();
              } catch (MalformedURLException e) {
                return null;
              }
            })
        .filter(url -> url != null)
        .distinct()
        .toArray(URL[]::new);
  }
}
