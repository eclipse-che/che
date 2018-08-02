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
package org.eclipse.che.maven.data;

import java.io.Serializable;

/**
 * Data class for org.apache.maven.model.ActivationOS
 *
 * @author Evgen Vidolob
 */
public class MavenActivationOS implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final String family;
  private final String arch;
  private final String version;

  public MavenActivationOS(String name, String family, String arch, String version) {
    this.name = name;
    this.family = family;
    this.arch = arch;
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public String getFamily() {
    return family;
  }

  public String getArch() {
    return arch;
  }

  public String getVersion() {
    return version;
  }
}
