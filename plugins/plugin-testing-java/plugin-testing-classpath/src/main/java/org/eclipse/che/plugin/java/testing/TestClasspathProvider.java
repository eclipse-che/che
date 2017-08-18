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

/**
 * Interface for defining test classpath providers for the test runner. All test classpath provider
 * implementations should implement this interface in order to register for the test runner service.
 *
 * @author Mirage Abeysekara
 */
@Deprecated
public interface TestClasspathProvider {

  /**
   * Returns the project class loader for executing test cases.
   *
   * @param projectAbsolutePath absolute path for the project location on the disk.
   * @param projectRelativePath path for the project relative to the workspace.
   * @param updateClasspath calculate the classpath if true. otherwise return existing class loader.
   * @return the class loader for the Java project.
   * @throws Exception when classloader creation failed.
   */
  ClassLoader getClassLoader(
      String projectAbsolutePath, String projectRelativePath, boolean updateClasspath)
      throws Exception;

  /**
   * String representation of the project type.
   *
   * @return the project type.
   */
  String getProjectType();
}
