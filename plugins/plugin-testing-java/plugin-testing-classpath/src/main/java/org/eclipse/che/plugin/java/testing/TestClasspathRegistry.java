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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Registry for test class path providers on the server.
 *
 * @author Mirage Abeysekara
 */
@Singleton
@Deprecated
public class TestClasspathRegistry {

  private final Map<String, TestClasspathProvider> classpathProviders = new HashMap<>();

  @Inject
  public TestClasspathRegistry(Set<TestClasspathProvider> testClasspathProviders) {
    testClasspathProviders.forEach(this::register);
  }

  /**
   * Get the classpath provider for a given project type.
   *
   * @param projectType string representation of the project type.
   * @return the TestClasspathProvider implementation for the project type if available, otherwise
   *     null.
   */
  public TestClasspathProvider getTestClasspathProvider(String projectType) {
    return classpathProviders.get(projectType);
  }

  private void register(@NotNull TestClasspathProvider provider) {
    classpathProviders.put(provider.getProjectType(), provider);
  }
}
