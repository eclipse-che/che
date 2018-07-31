/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Reference of remote repository in format: commitId referenceName.
 *
 * @author Vladyslav Zhukovskii
 */
@DTO
public interface RemoteReference {
  String getCommitId();

  void setCommitId(String commitId);

  RemoteReference withCommitId(String commitId);

  String getReferenceName();

  void setReferenceName(String referenceName);

  RemoteReference withReferenceName(String referenceName);
}
