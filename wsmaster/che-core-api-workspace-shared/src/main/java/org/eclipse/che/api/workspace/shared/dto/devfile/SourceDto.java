/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.dto.devfile;

import org.eclipse.che.api.core.model.workspace.devfile.Source;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leshchenko */
@DTO
public interface SourceDto extends Source {

  @Override
  String getType();

  void setType(String type);

  SourceDto withType(String type);

  @Override
  String getLocation();

  void setLocation(String location);

  SourceDto withLocation(String location);

  @Override
  String getBranch();

  void setBranch(String branch);

  SourceDto withBranch(String branch);

  @Override
  String getStartPoint();

  void setStartPoint(String startPoint);

  SourceDto withStartPoint(String startPoint);

  @Override
  String getTag();

  void setTag(String tag);

  SourceDto withTag(String tag);

  @Override
  String getCommitId();

  void setCommitId(String commitId);

  SourceDto withCommitId(String commitId);

  @Override
  String getSparseCheckoutDir();

  void setSparseCheckoutDir(String sparseCheckoutDir);

  SourceDto withSparseCheckoutDir(String sparseCheckoutDir);
}
