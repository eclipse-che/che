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
 * Request to checkout a branch or file(s) to the working tree.
 *
 * @author andrew00x
 */
@DTO
public interface CheckoutRequest {
  /** @return name of branch to checkout */
  String getName();

  void setName(String name);

  CheckoutRequest withName(String name);

  /**
   * @return name of a commit at which to start the new branch. If <code>null</code> the HEAD will
   *     be used
   */
  String getStartPoint();

  void setStartPoint(String startPoint);

  CheckoutRequest withStartPoint(String startPoint);

  /**
   * @return if <code>true</code> then create a new branch named {@link #name} and start it at
   *     {@link #startPoint} or to the HEAD if {@link #startPoint} is not set. If <code>false</code>
   *     and there is no branch with name {@link #name} corresponding exception will be thrown
   */
  boolean isCreateNew();

  void setCreateNew(boolean isCreateNew);

  CheckoutRequest withCreateNew(boolean isCreateNew);

  /** @return name of branch that will be tracked */
  String getTrackBranch();

  void setTrackBranch(String trackBranch);

  CheckoutRequest withTrackBranch(String trackBranch);

  /** @return list of files to checkout */
  List<String> getFiles();

  void setFiles(List<String> files);

  CheckoutRequest withFiles(List<String> files);

  void setNoTrack(boolean noTrack);

  /** @return indicates whether --no-track option should be applied during checkout */
  boolean isNoTrack();

  CheckoutRequest withNoTrack(boolean noTrack);
}
