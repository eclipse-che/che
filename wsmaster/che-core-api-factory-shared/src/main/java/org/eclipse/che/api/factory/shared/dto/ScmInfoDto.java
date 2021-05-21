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
package org.eclipse.che.api.factory.shared.dto;

import org.eclipse.che.api.core.model.factory.ScmInfo;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.JsonFieldName;

@DTO
public interface ScmInfoDto extends ScmInfo {

  @Override
  @JsonFieldName("scm_provider")
  String getScmProviderName();

  void setScmProviderName(String scmProviderName);

  ScmInfoDto withScmProviderName(String scmProviderName);

  @Override
  @JsonFieldName("clone_url")
  String getRepositoryUrl();

  void setRepositoryUrl(String repositoryUrl);

  ScmInfoDto withRepositoryUrl(String repositoryUrl);

  @Override
  String getBranch();

  void setBranch(String branch);

  ScmInfoDto withBranch(String branch);
}
