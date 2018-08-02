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
package org.eclipse.che.plugin.maven.server.core.project;

import java.util.List;
import java.util.Set;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.data.MavenProjectProblem;

/**
 * Result holder for MavenModelReader.
 *
 * @author Evgen Vidolob
 */
public class MavenModelReaderResult {
  private final MavenModel mavenModel;
  private final List<String> activeProfiles;
  private final List<String> inactiveProfiles;
  private final List<MavenProjectProblem> problems;
  private final Set<MavenKey> unresolvedArtifacts;

  public MavenModelReaderResult(
      MavenModel mavenModel,
      List<String> activeProfiles,
      List<String> inactiveProfiles,
      List<MavenProjectProblem> problems,
      Set<MavenKey> unresolvedArtifacts) {
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
