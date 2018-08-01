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
package org.eclipse.che.core.internal.resources;

/** @author Evgen Vidolob */
public class ResourceInfo {

  private int type;

  public ResourceInfo(int type) {
    this.type = type;
  }

  /** Returns the type setting for this info. Valid values are FILE, FOLDER, PROJECT, */
  public int getType() {
    return type;
  }
}
