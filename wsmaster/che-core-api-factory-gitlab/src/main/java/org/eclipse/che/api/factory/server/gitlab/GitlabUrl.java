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
package org.eclipse.che.api.factory.server.gitlab;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl;

/**
 * Representation of a gitlab URL, allowing to get details from it.
 *
 * <p>like https://gitlab.com/<username>/<repository>
 * https://gitlab.com/<username>/<repository>/-/tree/<branch>
 *
 * @author Max Shaposhnyk
 */
public class GitlabUrl implements RemoteFactoryUrl {

  /** Hostname of the gitlab URL */
  private String hostName;

  /** Username part of the gitlab URL */
  private String username;

  /** Project part of the gitlab URL */
  private String project;

  /** Repository part of the gitlab URL. */
  private String repository;

  /** Branch name */
  private String branch;

  /** Subfolder if any */
  private String subfolder;

  /** Devfile filenames list */
  private final List<String> devfileFilenames = new ArrayList<>();

  /**
   * Creation of this instance is made by the parser so user may not need to create a new instance
   * directly
   */
  protected GitlabUrl() {}

  /**
   * Gets hostname of this gitlab url
   *
   * @return the project part
   */
  public String getHostName() {
    return this.hostName;
  }

  public GitlabUrl withHostName(String hostName) {
    this.hostName = hostName;
    return this;
  }

  /**
   * Gets username of this gitlab url
   *
   * @return the username part
   */
  public String getUsername() {
    return this.username;
  }

  public GitlabUrl withUsername(String userName) {
    this.username = userName;
    return this;
  }

  /**
   * Gets project of this bitbucket url
   *
   * @return the project part
   */
  public String getProject() {
    return this.project;
  }

  public GitlabUrl withProject(String project) {
    this.project = project;
    return this;
  }

  /**
   * Gets repository of this gitlab url
   *
   * @return the repository part
   */
  public String getRepository() {
    return this.repository;
  }

  protected GitlabUrl withRepository(String repository) {
    this.repository = repository;
    return this;
  }

  protected GitlabUrl withDevfileFilenames(List<String> devfileFilenames) {
    this.devfileFilenames.addAll(devfileFilenames);
    return this;
  }

  /**
   * Gets branch of this gitlab url
   *
   * @return the branch part
   */
  public String getBranch() {
    return this.branch;
  }

  protected GitlabUrl withBranch(String branch) {
    if (!Strings.isNullOrEmpty(branch)) {
      this.branch = branch;
    }
    return this;
  }

  /**
   * Gets subfolder of this gitlab url
   *
   * @return the subfolder part
   */
  public String getSubfolder() {
    return this.subfolder;
  }

  /**
   * Sets the subfolder represented by the URL.
   *
   * @param subfolder path inside the repository
   * @return current gitlab URL instance
   */
  protected GitlabUrl withSubfolder(String subfolder) {
    this.subfolder = subfolder;
    return this;
  }

  /**
   * Provides list of configured devfile filenames with locations
   *
   * @return list of devfile filenames and locations
   */
  @Override
  public List<DevfileLocation> devfileFileLocations() {
    return devfileFilenames.stream().map(this::createDevfileLocation).collect(Collectors.toList());
  }

  private DevfileLocation createDevfileLocation(String devfileFilename) {
    return new DevfileLocation() {
      @Override
      public Optional<String> filename() {
        return Optional.of(devfileFilename);
      }

      @Override
      public String location() {
        return rawFileLocation(devfileFilename);
      }
    };
  }

  /**
   * Provides location to raw content of specified file
   *
   * @return location of specified file in a repository
   */
  public String rawFileLocation(String fileName) {
    StringJoiner joiner = new StringJoiner("/").add(hostName).add(username).add(project);
    if (repository != null) {
      joiner.add(repository);
    }
    joiner.add("-").add("raw").add(firstNonNull(branch, "master"));
    if (subfolder != null) {
      joiner.add(subfolder);
    }
    return joiner.add(fileName).toString();
  }

  /**
   * Provides location to the repository part of the full gitlab URL.
   *
   * @return location of the repository.
   */
  protected String repositoryLocation() {
    if (repository == null) {
      return hostName + "/" + this.username + "/" + this.project + ".git";
    } else {
      return hostName + "/" + this.username + "/" + this.project + "/" + repository + ".git";
    }
  }
}
