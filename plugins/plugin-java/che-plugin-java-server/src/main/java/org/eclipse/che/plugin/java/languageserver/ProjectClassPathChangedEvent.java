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
package org.eclipse.che.plugin.java.languageserver;

/**
 * Event signaling that the class path of a project might have changed
 *
 * @author Thomas Mäder
 */
public class ProjectClassPathChangedEvent {
  private final String path;

  public ProjectClassPathChangedEvent(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
