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
