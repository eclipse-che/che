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
package org.eclipse.che.maven.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/** Contains the information about profiles from the maven model. */
public class MavenExplicitProfiles implements Serializable {
  public static final MavenExplicitProfiles NONE =
      new MavenExplicitProfiles(Collections.emptySet());

  private Collection<String> myEnabledProfiles;
  private Collection<String> myDisabledProfiles;

  public MavenExplicitProfiles(
      Collection<String> enabledProfiles, Collection<String> disabledProfiles) {
    myEnabledProfiles = enabledProfiles;
    myDisabledProfiles = disabledProfiles;
  }

  public MavenExplicitProfiles(Collection<String> enabledProfiles) {
    this(enabledProfiles, Collections.emptySet());
  }

  /** Returns the list of profiles which are activated for applying. */
  public Collection<String> getEnabledProfiles() {
    return myEnabledProfiles;
  }

  /** Returns the list of profiles which are disabled and can be applied. */
  public Collection<String> getDisabledProfiles() {
    return myDisabledProfiles;
  }

  @Override
  public MavenExplicitProfiles clone() {
    return new MavenExplicitProfiles(
        new HashSet<>(myEnabledProfiles), new HashSet<>(myDisabledProfiles));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MavenExplicitProfiles that = (MavenExplicitProfiles) o;

    if (!myEnabledProfiles.equals(that.myEnabledProfiles)) return false;
    if (!myDisabledProfiles.equals(that.myDisabledProfiles)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myEnabledProfiles.hashCode();
    result = 31 * result + myDisabledProfiles.hashCode();
    return result;
  }
}
