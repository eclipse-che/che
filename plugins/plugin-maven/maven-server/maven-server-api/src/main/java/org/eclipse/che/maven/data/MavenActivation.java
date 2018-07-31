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
 * Data class for maven profile activation.
 *
 * @author Evgen Vidolob
 */
public class MavenActivation implements Serializable, Cloneable {
  private static final long serialVersionUID = 1L;

  private boolean activeByDefault;
  private MavenActivationOS os;
  private MavenActivationFile file;
  private MavenActivationProperty property;
  private String jdk;

  public boolean isActiveByDefault() {
    return activeByDefault;
  }

  public void setActiveByDefault(boolean activeByDefault) {
    this.activeByDefault = activeByDefault;
  }

  public MavenActivationOS getOs() {
    return os;
  }

  public void setOs(MavenActivationOS os) {
    this.os = os;
  }

  public MavenActivationFile getFile() {
    return file;
  }

  public void setFile(MavenActivationFile file) {
    this.file = file;
  }

  public MavenActivationProperty getProperty() {
    return property;
  }

  public void setProperty(MavenActivationProperty property) {
    this.property = property;
  }

  public String getJdk() {
    return jdk;
  }

  public void setJdk(String jdk) {
    this.jdk = jdk;
  }

  @Override
  protected MavenActivation clone() throws CloneNotSupportedException {
    return (MavenActivation) super.clone();
  }
}
