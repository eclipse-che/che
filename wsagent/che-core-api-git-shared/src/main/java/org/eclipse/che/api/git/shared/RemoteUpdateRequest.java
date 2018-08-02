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
package org.eclipse.che.api.git.shared;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Request to update tracked repositories.
 *
 * @author andrew00x
 */
@DTO
public interface RemoteUpdateRequest {
  /** @return remote name */
  String getName();

  void setName(String name);

  /** @return list tracked branches */
  List<String> getBranches();

  void setBranches(List<String> branches);

  /**
   * @return if <code>true</code> then {@link #branches} instead of replacing the list of currently
   *     tracked branches, added to that list
   */
  boolean isAddBranches();

  void setAddBranches(boolean isAddBranches);

  /** @return remote URLs to be added */
  List<String> getAddUrl();

  void setAddUrl(List<String> addUrl);

  /** @return remote URLs to be removed */
  List<String> getRemoveUrl();

  void setRemoveUrl(List<String> removeUrl);

  /** @return remote push URLs to be added */
  List<String> getAddPushUrl();

  void setAddPushUrl(List<String> addPushUrl);

  /** @return remote push URLs to be removed */
  List<String> getRemovePushUrl();

  void setRemovePushUrl(List<String> removePushUrl);
}
