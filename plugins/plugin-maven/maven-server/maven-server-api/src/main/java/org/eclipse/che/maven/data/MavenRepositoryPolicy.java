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
 * Data class for org.apache.maven.model.RepositoryPolicy
 *
 * @author Evgen Vidolob
 */
public class MavenRepositoryPolicy implements Serializable {
  private static final long serialVersionUID = 1L;

  private final boolean enabled;
  private final String updatePolicy;
  private final String checksumPolicy;

  public MavenRepositoryPolicy(boolean enabled, String updatePolicy, String checksumPolicy) {
    this.enabled = enabled;
    this.updatePolicy = updatePolicy;
    this.checksumPolicy = checksumPolicy;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getUpdatePolicy() {
    return updatePolicy;
  }

  public String getChecksumPolicy() {
    return checksumPolicy;
  }
}
