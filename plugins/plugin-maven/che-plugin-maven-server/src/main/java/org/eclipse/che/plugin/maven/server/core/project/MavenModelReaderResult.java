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
package org.eclipse.che.plugin.maven.server.core.project;

import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.data.MavenProjectProblem;

import java.util.List;
import java.util.Set;

/**
 * Result holder for MavenModelReader.
 *
 * @author Evgen Vidolob
 */
public class MavenModelReaderResult {
    private final MavenModel                mavenModel;
    private final List<String>              activeProfiles;
    private final List<String>              inactiveProfiles;
    private final List<MavenProjectProblem> problems;
    private final Set<MavenKey>             unresolvedArtifacts;

    public MavenModelReaderResult(MavenModel mavenModel, List<String> activeProfiles, List<String> inactiveProfiles,
                                  List<MavenProjectProblem> problems, Set<MavenKey> unresolvedArtifacts) {
        this.mavenModel = mavenModel;
        this.activeProfiles = activeProfiles;
        this.inactiveProfiles = inactiveProfiles;
        this.problems = problems;
        this.unresolvedArtifacts = unresolvedArtifacts;
    }

    public Set<MavenKey> getUnresolvedArtifacts() {
        return unresolvedArtifacts;
    }

    public MavenModel getMavenModel() {
        return mavenModel;
    }

    public List<String> getActiveProfiles() {
        return activeProfiles;
    }

    public List<String> getInactiveProfiles() {
        return inactiveProfiles;
    }

    public List<MavenProjectProblem> getProblems() {
        return problems;
    }
}
