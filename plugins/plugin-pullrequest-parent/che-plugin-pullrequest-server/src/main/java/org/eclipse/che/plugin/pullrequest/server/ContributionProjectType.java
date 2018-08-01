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
package org.eclipse.che.plugin.pullrequest.server;

import static org.eclipse.che.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_DISPLAY_NAME;
import static org.eclipse.che.plugin.pullrequest.shared.ContributionProjectTypeConstants.CONTRIBUTION_PROJECT_TYPE_ID;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.plugin.pullrequest.shared.ContributionProjectTypeConstants;

/**
 * The contribution project type definition.
 *
 * @author Kevin Pollet
 */
@Singleton
public class ContributionProjectType extends ProjectTypeDef {
  @Inject
  public ContributionProjectType() {
    super(CONTRIBUTION_PROJECT_TYPE_ID, CONTRIBUTION_PROJECT_TYPE_DISPLAY_NAME, false, true);

    addVariableDefinition(
        ContributionProjectTypeConstants.CONTRIBUTE_LOCAL_BRANCH_NAME,
        "Name of local branch",
        false);
    addVariableDefinition(
        ContributionProjectTypeConstants.CONTRIBUTE_TO_BRANCH_VARIABLE_NAME,
        "Branch where the contribution has to be pushed",
        true);
  }
}
