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
package org.eclipse.che.plugin.openshift.client;

import java.util.Date;
import org.eclipse.che.plugin.docker.client.json.Version;

public class OpenShiftVersion {
  private String major;
  private String minor;
  private String gitVersion;
  private String gitTreeState;
  private Date buildDate;
  private String goVersion;
  private String gitCommit;
  private String compiler;
  private String platform;

  public String getMajor() {
    return major;
  }

  public void setMajor(String major) {
    this.major = major;
  }

  public String getMinor() {
    return minor;
  }

  public void setMinor(String minor) {
    this.minor = minor;
  }

  public String getGitVersion() {
    return gitVersion;
  }

  public void setGitVersion(String gitVersion) {
    this.gitVersion = gitVersion;
  }

  public String getGitTreeState() {
    return gitTreeState;
  }

  public void setGitTreeState(String gitTreeState) {
    this.gitTreeState = gitTreeState;
  }

  public Date getBuildDate() {
    return buildDate;
  }

  public void setBuildDate(Date buildDate) {
    this.buildDate = buildDate;
  }

  public String getGoVersion() {
    return goVersion;
  }

  public void setGoVersion(String goVersion) {
    this.goVersion = goVersion;
  }

  public String getGitCommit() {
    return gitCommit;
  }

  public void setGitCommit(String gitCommit) {
    this.gitCommit = gitCommit;
  }

  public String getCompiler() {
    return compiler;
  }

  public void setCompiler(String compiler) {
    this.compiler = compiler;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public Version getVersion() {
    Version version = new Version();
    version.setVersion(major + "." + minor);
    version.setGitCommit(getGitCommit());
    version.setGoVersion(getGoVersion());
    if (getPlatform() != null) {
      String[] elements = getPlatform().split("/");
      if (elements.length == 2) {
        version.setOs(elements[0]);
        version.setArch(elements[1]);
      }
    }
    return version;
  }
}
