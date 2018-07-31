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
package org.eclipse.che.ide.api.extension;

/**
 * Describes Dependency information of Extension.
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
public class DependencyDescription {
  private String id;

  private String version;

  /**
   * Create {@link DependencyDescription} instance
   *
   * @param id
   * @param version
   */
  public DependencyDescription(String id, String version) {
    this.id = id;
    this.version = version;
  }

  /**
   * Get required extension id
   *
   * @return
   */
  public String getId() {
    return id;
  }

  /**
   * Get version of the used dependency
   *
   * @return
   */
  public String getVersion() {
    return version;
  }
}
