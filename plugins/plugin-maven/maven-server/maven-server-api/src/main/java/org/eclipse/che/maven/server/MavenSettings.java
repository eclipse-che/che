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
package org.eclipse.che.maven.server;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;

/**
 * Setting for maven server. Contains path to maven, local repository path etc.
 *
 * @author Evgen Vidolob
 */
public class MavenSettings implements Serializable {
  private static final long serialVersionUID = 1L;

  private File mavenHome;
  private File userSettings;
  private File globalSettings;
  private File localRepository;
  private Properties userProperties;
  private int loggingLevel;
  private boolean isOffline;

  public MavenSettings() {
    userProperties = new Properties();
  }

  public File getMavenHome() {
    return mavenHome;
  }

  public void setMavenHome(File mavenHome) {
    this.mavenHome = mavenHome;
  }

  public File getUserSettings() {
    return userSettings;
  }

  public void setUserSettings(File userSettings) {
    this.userSettings = userSettings;
  }

  public File getGlobalSettings() {
    return globalSettings;
  }

  public void setGlobalSettings(File globalSettings) {
    this.globalSettings = globalSettings;
  }

  public File getLocalRepository() {
    return localRepository;
  }

  public void setLocalRepository(File localRepository) {
    this.localRepository = localRepository;
  }

  public Properties getUserProperties() {
    return userProperties;
  }

  public void setUserProperties(Properties userProperties) {
    this.userProperties = userProperties;
  }

  public int getLoggingLevel() {
    return loggingLevel;
  }

  public void setLoggingLevel(int loggingLevel) {
    this.loggingLevel = loggingLevel;
  }

  public boolean isOffline() {
    return isOffline;
  }

  public void setOffline(boolean offline) {
    isOffline = offline;
  }
}
