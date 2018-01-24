/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
