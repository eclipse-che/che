/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import org.eclipse.che.api.installer.server.exception.IllegalInstallerKeyException;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * @author Anatolii Bazko
 * @author Alexander Garagatyi
 */
public class InstallerFqn implements Serializable {
  public static final String LATEST_VERSION_TAG = "latest";

  private String id;
  private String version;

  public InstallerFqn() {}

  public InstallerFqn(String id) {
    this(id, null);
  }

  public InstallerFqn(String id, @Nullable String version) {
    this.id = id;
    this.version = version == null ? LATEST_VERSION_TAG : version;
  }

  public String getId() {
    return id;
  }

  public String getVersion() {
    return version;
  }

  /**
   * Factory method. Installer key is basically a string meeting the format: {@code id:version}. The
   * version part can be omitted.
   *
   * @throws IllegalInstallerKeyException in case of wrong format
   */
  public static InstallerFqn parse(String installerKey) throws IllegalInstallerKeyException {
    String[] parts = installerKey.split(":");

    if (parts.length == 1) {
      return new InstallerFqn(parts[0], null);
    } else if (parts.length == 2) {
      return new InstallerFqn(parts[0], parts[1]);
    } else {
      throw new IllegalInstallerKeyException("Illegal installer key format: " + installerKey);
    }
  }

  /** Factory method for fetching fqn of installer. */
  public static InstallerFqn of(Installer installer) {
    return new InstallerFqn(installer.getId(), installer.getVersion());
  }

  public String toKey() {
    return id + ":" + version;
  }

  public boolean hasLatestTag() {
    return LATEST_VERSION_TAG.equals(version);
  }

  /** Indicates if installer id is contained in the giving list of keys. */
  public static boolean idInKeyList(
      String installerId, @Nullable Collection<String> installerKeys) {
    Objects.requireNonNull(installerId, "Installer ID is null");
    if (installerKeys == null) {
      return false;
    }

    for (String installerKey : installerKeys) {
      if (installerKey.equals(installerId) || installerKey.startsWith(installerId + ":")) {
        return true;
      }
    }

    return false;
  }

  /** Indicates if installer id is contained in the giving list of FQNs. */
  public static boolean idInFqnList(
      String installerId, @Nullable Collection<InstallerFqn> installerFqns) {
    Objects.requireNonNull(installerId, "Installer ID is null");
    if (installerFqns == null) {
      return false;
    }

    for (InstallerFqn fqn : installerFqns) {
      if (fqn.getId().equals(installerId)) {
        return true;
      }
    }

    return false;
  }

  /** Indicates if installer id is contained in the giving list of installers. */
  public static boolean idInInstallerList(
      String installerId, @Nullable Collection<? extends Installer> installers) {
    Objects.requireNonNull(installerId, "Installer ID is null");
    if (installers == null) {
      return false;
    }

    for (Installer installer : installers) {
      if (installer.getId().equals(installerId)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InstallerFqn)) return false;
    InstallerFqn installerFqn = (InstallerFqn) o;
    return Objects.equals(id, installerFqn.id) && Objects.equals(version, installerFqn.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, version);
  }

  @Override
  public String toString() {
    return id + ":" + version;
  }
}
