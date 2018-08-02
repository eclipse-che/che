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

import java.util.Collections;
import java.util.List;

/**
 * Data class for maven model.
 *
 * @author Evgen Vidolob
 */
public class MavenModel extends MavenModelBase {
  private static final long serialVersionUID = 1L;

  private MavenKey mavenKey;
  private MavenParent parent;
  private String packaging;
  private String name;

  private List<MavenProfile> profiles = Collections.emptyList();
  private MavenBuild build = new MavenBuild();

  public MavenKey getMavenKey() {
    return mavenKey;
  }

  public void setMavenKey(MavenKey mavenKey) {
    this.mavenKey = mavenKey;
  }

  public MavenParent getParent() {
    return parent;
  }

  public void setParent(MavenParent parent) {
    this.parent = parent;
  }

  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<MavenProfile> getProfiles() {
    return profiles;
  }

  public void setProfiles(List<MavenProfile> profiles) {
    this.profiles = profiles;
  }

  public MavenBuild getBuild() {
    return build;
  }

  @Override
  public String toString() {
    return "MavenModel{"
        + "mavenKey="
        + mavenKey
        + ", parent="
        + parent
        + ", packaging='"
        + packaging
        + '\''
        + ", name='"
        + name
        + '\''
        + ", profiles="
        + profiles
        + ", build="
        + build
        + '}';
  }
}
