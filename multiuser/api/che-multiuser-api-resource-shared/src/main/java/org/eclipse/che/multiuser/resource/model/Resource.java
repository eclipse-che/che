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

/**
 * Represents some number of resources that can be used by account.
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
public interface Resource {
  /** Returns type of resources, e.g. RAM. */
  String getType();

  /**
   * Returns amount of resources.
   *
   * <p>Applicable values here are from -1 to {@link Long#MAX_VALUE} inclusively. -1 value represent
   * infinity.
   */
  long getAmount();

  /** Returns unit of resources. */
  String getUnit();
}
