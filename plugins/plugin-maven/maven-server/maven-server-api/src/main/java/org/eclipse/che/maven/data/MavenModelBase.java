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
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Data class for org.apache.maven.model.ModelBase
 *
 * @author Evgen Vidolob
 */
public class MavenModelBase implements Serializable {
  private static final long serialVersionUID = 1L;

  private Properties properties;

  private List<MavenPlugin> plugins = Collections.emptyList();
  private List<MavenArtifact> extensions = Collections.emptyList();
  private List<MavenArtifact> dependencies = Collections.emptyList();
  private List<MavenRemoteRepository> remoteRepositories = Collections.emptyList();

  private List<String> modules;

  public Properties getProperties() {
    if (properties == null) {
      properties = new Properties();
    }
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public List<MavenPlugin> getPlugins() {
    return plugins;
  }

  public void setPlugins(List<MavenPlugin> plugins) {
    this.plugins = plugins;
  }

  public List<MavenArtifact> getExtensions() {
    return extensions;
  }

  public void setExtensions(List<MavenArtifact> extensions) {
    this.extensions = extensions;
  }

  public List<MavenArtifact> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<MavenArtifact> dependencies) {
    this.dependencies = dependencies;
  }

  public List<MavenRemoteRepository> getRemoteRepositories() {
    return remoteRepositories;
  }

  public void setRemoteRepositories(List<MavenRemoteRepository> remoteRepositories) {
    this.remoteRepositories = remoteRepositories;
  }

  public List<String> getModules() {
    return modules;
  }

  public void setModules(List<String> modules) {
    this.modules = modules;
  }
}
