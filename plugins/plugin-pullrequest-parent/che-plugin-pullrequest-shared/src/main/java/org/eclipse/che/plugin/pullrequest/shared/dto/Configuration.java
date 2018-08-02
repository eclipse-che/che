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
package org.eclipse.che.plugin.pullrequest.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/** Contribution configuration, which contains the values chosen by the user. */
@DTO
public interface Configuration {
  String getContributionBranchName();

  Configuration withContributionBranchName(String name);

  String getContributionComment();

  Configuration withContributionComment(String comment);

  String getContributionTitle();

  Configuration withContributionTitle(String title);
}
