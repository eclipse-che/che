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
package org.eclipse.che.ide.ext.java.client.project.classpath.service;

import com.google.inject.ImplementedBy;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;

import java.util.List;

/**
 * Interface for the service which gets information about classpath.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(ClasspathServiceClientImpl.class)
public interface ClasspathServiceClient {
    /**
     * Returns information about classpath.
     *
     * @param projectPath
     *         path to the current project
     * @return list of classpath entries
     */
    Promise<List<ClasspathEntryDto>> getClasspath(String projectPath);

}
