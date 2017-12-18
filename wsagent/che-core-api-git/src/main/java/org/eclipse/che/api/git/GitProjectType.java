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
package org.eclipse.che.api.git;

import static org.eclipse.che.api.project.shared.Constants.VCS_PROVIDER_NAME;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.project.server.type.TransientMixin;

/** @author Vitaly Parfonov */
@Singleton
public class GitProjectType extends TransientMixin {

  public static final String TYPE_ID = "git";
  public static final String DISPLAY_NAME = "git";
  public static final String GIT_CURRENT_HEAD_NAME = "git.current.head.name";
  public static final String GIT_REPOSITORY_REMOTES = "git.repository.remotes";

  @Inject
  public GitProjectType(GitValueProviderFactory gitRepositoryValueProviderFactory) {
    super(TYPE_ID, DISPLAY_NAME);
    addVariableDefinition(
        VCS_PROVIDER_NAME, "Is this git repo or not?", true, gitRepositoryValueProviderFactory);
    addVariableDefinition(
        GIT_CURRENT_HEAD_NAME,
        "Name of current git branch or revision",
        false,
        gitRepositoryValueProviderFactory);
    addVariableDefinition(
        GIT_REPOSITORY_REMOTES,
        "List of git repository remote addresses",
        false,
        gitRepositoryValueProviderFactory);
  }
}
