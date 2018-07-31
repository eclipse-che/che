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
 * Request to create new tag.
 *
 * @author andrew00x
 */
@DTO
public interface TagCreateRequest {
  /** @return name of tag to create */
  String getName();

  void setName(String name);

  /** @return commit to make tag. If <code>null</code> then HEAD is used */
  String getCommit();

  void setCommit(String commit);

  /** @return message for tag */
  String getMessage();

  void setMessage(String message);

  /** @return force create tag operation */
  boolean isForce();

  void setForce(boolean isForce);
}
