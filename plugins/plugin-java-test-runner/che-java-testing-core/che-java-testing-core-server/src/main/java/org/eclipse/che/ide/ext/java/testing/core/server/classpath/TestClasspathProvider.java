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

/**
 * Interface for defining test classpath providers for the test runner. All test classpath provider implementations
 * should implement this interface in order to register for the test runner service.
 *
 * @author Mirage Abeysekara
 */
public interface TestClasspathProvider {

    /**
     * Returns the project class loader for executing test cases.
     *
     * @param projectPath     absolute path for the project location.
     * @param updateClasspath calculate the classpath if true. otherwise return existing class loader.
     * @return the class loader for the Java project.
     * @throws Exception when classloader creation failed.
     */
    ClassLoader getClassLoader(String projectPath, boolean updateClasspath) throws Exception;

    /**
     * String representation of the project type.
     *
     * @return the project type.
     */
    String getProjectType();
}
