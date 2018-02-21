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
package org.eclipse.che.plugin.maven.server.projecttype.handler;

import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.prefixURI;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateWorkspaceParameters;
import org.eclipse.che.plugin.java.languageserver.WorkspaceSynchronizer;

/** @author Vitaly Parfonov */
@Singleton
public class MavenProjectInitHandler implements ProjectInitHandler {

  private final WorkspaceSynchronizer workspaceSynchronizer;

  @Inject
  public MavenProjectInitHandler(WorkspaceSynchronizer workspaceSynchronizer) {
    this.workspaceSynchronizer = workspaceSynchronizer;
  }

  @Override
  public String getProjectType() {
    return MAVEN_ID;
  }

  @Override
  public void onProjectInitialized(String projectFolder)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException {

    UpdateWorkspaceParameters params = new UpdateWorkspaceParameters();
    params.setAddedProjectsUri(Collections.singletonList(prefixURI(projectFolder)));

    workspaceSynchronizer.syncronizerWorkspaceAsync(params);
  }
}
