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

/**
 * Data class for maven profile.
 *
 * @author Evgen Vidolob
 */
public class MavenProfile extends MavenModelBase {
  private static final long serialVersionUID = 1L;

  private final String id;
  private final String source;
  private final MavenBuild build = new MavenBuild();

  private MavenActivation activation;

  public MavenProfile(String id, String source) {
    this.id = id;
    this.source = source;
  }

  public String getId() {
    return id;
  }

  public String getSource() {
    return source;
  }

  public MavenBuild getBuild() {
    return build;
  }

  public MavenActivation getActivation() {
    return activation;
  }

  public void setActivation(MavenActivation activation) {
    this.activation = activation;
  }
}
