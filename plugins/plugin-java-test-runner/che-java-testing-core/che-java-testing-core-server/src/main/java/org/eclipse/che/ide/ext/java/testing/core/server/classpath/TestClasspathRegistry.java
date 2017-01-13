/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.testing.core.server.classpath;

import com.google.inject.Inject;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for test class path providers on the server.
 *
 * @author Mirage Abeysekara
 */
@Singleton
public class TestClasspathRegistry {

    private final Map<String, TestClasspathProvider> classpathProviders = new HashMap<>();


    @Inject
    public TestClasspathRegistry(Set<TestClasspathProvider> testClasspathProviders) {
        testClasspathProviders.forEach(this::register);
    }

    private void register(@NotNull TestClasspathProvider provider) {
        classpathProviders.put(provider.getProjectType(), provider);
    }

    /**
     * Get the classpath provider for a given project type.
     *
     * @param projectType string representation of the project type.
     * @return the TestClasspathProvider implementation for the project type if available, otherwise null.
     */
    public TestClasspathProvider getTestClasspathProvider(String projectType) {
        return classpathProviders.get(projectType);
    }

}
