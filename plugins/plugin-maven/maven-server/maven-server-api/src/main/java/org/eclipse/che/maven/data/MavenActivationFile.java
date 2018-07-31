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
package org.eclipse.che.maven.data;

import java.io.Serializable;

/**
 * Data class for maven file activation.
 *
 * @author Evgen Vidolob
 */
public class MavenActivationFile implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String exist;
  private final String missing;

  public MavenActivationFile(String exist, String missing) {

    this.exist = exist;
    this.missing = missing;
  }

  public String getExist() {
    return exist;
  }

  public String getMissing() {
    return missing;
  }
}
