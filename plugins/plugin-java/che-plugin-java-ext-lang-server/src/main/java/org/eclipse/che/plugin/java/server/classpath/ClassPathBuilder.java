/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.classpath;

import org.eclipse.che.ide.ext.java.shared.dto.ClassPathBuilderResult;

import java.util.concurrent.ExecutionException;

/**
 * The build classpath specifies which Java source files and resource files in a project are considered by the Java builder and specifies
 * how to find types outside of the project.
 *
 * @author Valeriy Svydenko
 */
public interface ClassPathBuilder {

    /**
     * Builds classpath for the current project.
     *
     * @param projectPath
     *         relative path to current project from the workspace
     * @return information about building project classpath
     * @throws ExecutionException
     *         if the computation threw an exception
     * @throws InterruptedException
     *         if the current thread was interrupted
     */
    ClassPathBuilderResult buildClassPath(String projectPath) throws ExecutionException, InterruptedException;
}
