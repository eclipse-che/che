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

import org.eclipse.che.dto.shared.DTO;

/**
 * Git branch description.
 *
 * @author andrew00x
 */
@DTO
public interface Branch {
  /** @return full name of branch, e.g. 'refs/heads/master' */
  String getName();

  /** @return <code>true</code> if branch is checked out and false otherwise */
  boolean isActive();

  /** @return display name of branch, e.g. 'refs/heads/master' -> 'master' */
  String getDisplayName();

  /** @return <code>true</code> if branch is a remote branch */
  boolean isRemote();

  Branch withName(String name);

  Branch withDisplayName(String displayName);

  Branch withActive(boolean isActive);

  Branch withRemote(boolean isRemote);
}
