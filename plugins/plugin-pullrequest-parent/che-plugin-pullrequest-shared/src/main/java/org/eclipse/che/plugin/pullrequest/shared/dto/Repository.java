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
package org.eclipse.che.plugin.pullrequest.shared.dto;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface Repository {
  String getName();

  Repository withName(String name);

  String getCloneUrl();

  Repository withCloneUrl(String cloneUrl);

  boolean isFork();

  Repository withFork(boolean isFork);

  boolean isPrivateRepo();

  Repository withPrivateRepo(boolean isPrivateRepo);

  Repository getParent();

  Repository withParent(Repository parent);
}
