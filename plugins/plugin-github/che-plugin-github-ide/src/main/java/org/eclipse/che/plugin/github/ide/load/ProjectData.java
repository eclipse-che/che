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
package org.eclipse.che.plugin.github.ide.load;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: ProjectData.java Nov 18, 2011 3:27:38 PM vereshchaka $
 */
public class ProjectData {
  private String name;
  private String description;
  private String type;
  /** Url to clone from GitHub. */
  private String repositoryUrl;
  /** Url to clone from GitHub (readOnly). */
  private String readOnlyUrl;

  private String httpTransportUrl;
  private boolean isPrivateRepo;

  private List<String> targets;

  public ProjectData(
      String name,
      String description,
      String type,
      List<String> targets,
      String repositoryUrl,
      String readOnlyUrl,
      String httpTransportUrl,
      boolean isPrivateRepo) {
    this.name = name;
    this.description = description;
    this.type = type;
    this.repositoryUrl = repositoryUrl;
    this.targets = targets;
    this.readOnlyUrl = readOnlyUrl;
    this.httpTransportUrl = httpTransportUrl;
    this.isPrivateRepo = isPrivateRepo;
  }

  /**
   * Get the url to clone from GitHub.
   *
   * @return the repositoryUrl
   */
  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  /** @param repositoryUrl the repositoryUrl to set */
  public void setRepositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
  }

  /** @return the name */
  public String getName() {
    return name;
  }

  /** @return the description */
  public String getDescription() {
    return description;
  }

  /** @return the type */
  public String getType() {
    return type;
  }

  /** @param name the name to set */
  public void setName(String name) {
    this.name = name;
  }

  public void setType(String type) {
    this.type = type;
  }

  /** @return the targets */
  public List<String> getTargets() {
    if (targets == null) {
      targets = new ArrayList<>();
    }
    return targets;
  }

  /** @param targets the targets to set */
  public void setTargets(List<String> targets) {
    this.targets = targets;
  }

  /** @param description the description to set */
  public void setDescription(String description) {
    this.description = description;
  }

  public String getReadOnlyUrl() {
    return readOnlyUrl;
  }

  public void setReadOnlyUrl(String readOnlyUrl) {
    this.readOnlyUrl = readOnlyUrl;
  }

  /**
   * Gets the HTTPS URL to the repository, such as "https://github.com/eclipse/che.git" This URL is
   * read-only.
   */
  public String getHttpTransportUrl() {
    return httpTransportUrl;
  }

  /** Sets the HTTPS URL to the repository, such as "https://github.com/eclipse/che.git" */
  public void setHttpTransportUrl(String httpTransportUrl) {
    this.httpTransportUrl = httpTransportUrl;
  }

  /**
   * Gets state of the repository.
   *
   * @return {@code true} when the repository is private, {@code false} otherwise
   */
  public boolean isPrivateRepo() {
    return isPrivateRepo;
  }

  /**
   * Sets state of the repository.
   *
   * @param isPrivateRepo should be {@code true} when the repository is private, {@code false}
   *     otherwise
   */
  void setPrivateRepo(boolean isPrivateRepo) {
    this.isPrivateRepo = isPrivateRepo;
  }
}
