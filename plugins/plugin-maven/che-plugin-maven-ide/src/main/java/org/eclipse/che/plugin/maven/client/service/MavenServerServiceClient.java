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
package org.eclipse.che.plugin.maven.client.service;

import com.google.inject.ImplementedBy;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;

import java.util.List;

/**
 * Client for Maven Server API.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(MavenServerServiceClientImpl.class)
public interface MavenServerServiceClient {

    /**
     * Returns effective pom.
     *
     * @param projectPath
     *         path to current project
     * @return content of the effective pom
     */
    Promise<String> getEffectivePom(String projectPath);

    /**
     * Invokes downloading sources.
     *
     * @param projectPath the project path
     * @param fqn the FQN for class file
     * @return true if downloading was successful, false otherwise
     */
    Promise<Boolean> downloadSources(String projectPath, String fqn);

    /**
     * Invokes reimporting maven dependencies.
     *
     * @param projectsPaths
     *         the paths to projects which need to be re-imported maven model
     */
    Promise<Void> reImportProjects(List<String> projectsPaths);

    /**
     * Invokes reconciling for pom.xml file
     * @param pomPath tha path to pom.xml file
     * @return list of problems if any
     */
    Promise<List<Problem>> reconcilePom(String pomPath);
}
