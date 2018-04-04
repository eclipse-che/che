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
