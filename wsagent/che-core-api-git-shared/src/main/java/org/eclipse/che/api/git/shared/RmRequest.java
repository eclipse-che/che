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
 * Request to remove files.
 *
 * @author andrew00x
 */
@DTO
public interface RmRequest {
  /** @return files to remove */
  List<String> getItems();

  void setItems(List<String> items);

  RmRequest withItems(List<String> items);

  /** @return is RmRequest represents remove from index only */
  boolean isCached();

  void setCached(boolean isCached);

  RmRequest withCached(boolean cached);

  boolean isRecursively();

  void setRecursively(boolean isRecursively);

  RmRequest withRecursively(boolean isRecursively);
}
