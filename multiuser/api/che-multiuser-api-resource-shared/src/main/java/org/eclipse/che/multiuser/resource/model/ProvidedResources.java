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
package org.eclipse.che.multiuser.resource.model;

import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Resources that are provided for using by account by some resource providing mechanism.
 *
 * @author Sergii Leschenko
 */
public interface ProvidedResources {

  /** Returns id of resource provider. */
  String getProviderId();

  /**
   * Returns id of granted resource entity. Can be null when provider provides static single entry.
   */
  @Nullable
  String getId();

  /** Returns owner of resources. */
  String getOwner();

  /** Returns time when resources became active. */
  Long getStartTime();

  /** Returns time when resources will be/became inactive. */
  Long getEndTime();

  /** Returns list of resources which can be used by owner. */
  List<? extends Resource> getResources();
}
